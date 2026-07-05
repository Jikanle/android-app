import { assertEquals, assertStringIncludes } from "jsr:@std/assert@1";
import { buildMusicPrompt, handler } from "./index.ts";

Deno.test("music prompt requests a distinct composition", () => {
  const prompt = buildMusicPrompt("es", [
    { translated_text: "Cae la flor bajo la luna", emotion: "nostalgia" },
  ]);
  assertStringIncludes(
    prompt,
    "Original Spanish song with a distinct new melody",
  );
  assertStringIncludes(prompt, "Do not imitate any existing artist");
  assertStringIncludes(prompt, "Cae la flor bajo la luna");
});

Deno.test("generate-song rejects unsupported methods before spending", async () => {
  const response = await handler(
    new Request("https://example.test", { method: "GET" }),
  );
  assertEquals(response.status, 405);
  assertEquals(await response.json(), { error: "method_not_allowed" });
});
