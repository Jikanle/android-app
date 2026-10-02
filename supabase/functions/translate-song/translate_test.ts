import { assertEquals, assertThrows } from "https://deno.land/std@0.224.0/assert/mod.ts";
import { isLang, parseTranslatedLines, translateLines } from "./translate.ts";

const SAMPLE = {
  lines: [
    { source: "さくら さくら", target: "cerezos en flor", source_units: 4, target_units: 5,
      emotion: "asombro", stressed: "cerezos", note: "open -o- on the held note" },
    { source: "野山も里も", target: "montes y aldeas", source_units: 6, target_units: 6,
      emotion: "amplitud", stressed: "montes", note: "exact count" },
  ],
};

function stubAnthropic(text: string): () => void {
  const original = globalThis.fetch;
  globalThis.fetch = () =>
    Promise.resolve(new Response(JSON.stringify({ content: [{ type: "text", text }] }), { status: 200 }));
  return () => { globalThis.fetch = original; };
}

Deno.test("isLang includes Portuguese without claiming other pairs are ready", () => {
  for (const l of ["ja", "en", "es", "zh", "pt"]) assertEquals(isLang(l), true);
  for (const l of ["fr", "", "JA", 3]) assertEquals(isLang(l), false);
});

Deno.test("translateLines parses clean JSON", async () => {
  const restore = stubAnthropic(JSON.stringify(SAMPLE));
  try {
    const out = await translateLines(["さくら さくら", "野山も里も"], "ja", "es", "test-key");
    assertEquals(out.length, 2);
    assertEquals(out[0].target, "cerezos en flor");
    assertEquals(out[1].target_units, 6);
  } finally {
    restore();
  }
});

Deno.test("translateLines tolerates prose/fences around the JSON", async () => {
  const wrapped = "Here you go:\n```json\n" + JSON.stringify(SAMPLE) + "\n```\nHope that helps!";
  const restore = stubAnthropic(wrapped);
  try {
    const out = await translateLines(["さくら さくら", "野山も里も"], "ja", "es", "test-key");
    assertEquals(out[0].emotion, "asombro");
  } finally {
    restore();
  }
});

Deno.test("rejects omissions and reordered source lines", () => {
  assertThrows(() => parseTranslatedLines(JSON.stringify(SAMPLE), ["さくら さくら"]));
  assertThrows(() => parseTranslatedLines(JSON.stringify(SAMPLE), ["野山も里も", "さくら さくら"]));
});

Deno.test("rejects untyped unit estimates and empty target", () => {
  for (const patch of [{ target_units: "5" }, { target_units: -1 }, { target: " " }]) {
    const lines = [{ ...SAMPLE.lines[0], ...patch }];
    assertThrows(() => parseTranslatedLines(JSON.stringify({ lines }), ["さくら さくら"]));
  }
});

Deno.test("Portuguese output keeps repeated lines as distinct positions", () => {
  const lines = Array.from({ length: 2 }, () => ({ ...SAMPLE.lines[0], target: "Cerejeiras, cerejeiras" }));
  assertEquals(parseTranslatedLines(JSON.stringify({ lines }), ["さくら さくら", "さくら さくら"]).length, 2);
});
