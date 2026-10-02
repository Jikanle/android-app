import { assertEquals, assertThrows } from "https://deno.land/std@0.224.0/assert/mod.ts";
import { parseStudy, type Study, studyPreview } from "./study.ts";
const study: Study = {
  schemaVersion: 1, id: "synthetic-test", title: "Original test phrase", artist: "Test author",
  sourceLanguage: "ja", targetLanguage: "pt", provenanceUrl: "https://example.org/test",
  permission: "private_text_review",
  lines: [{ id: "line-0", original: "こんにちは", draft: "Olá", note: "Synthetic fixture, not a lyric" }],
  music: { audioReference: null, excerptStartMs: null, excerptEndMs: null },
};
Deno.test("preview maintains identity and never fabricates timings or unit counts", () => {
  const preview = studyPreview(parseStudy(study));
  assertEquals(preview.translations[0].targetLanguage, "pt");
  assertEquals(preview.lyrics[0].lineIndex, 0);
  assertEquals("sourceUnits" in preview.translations[0].lines[0], false);
});
Deno.test("rejects duplicate lines and ungrounded timing", () => {
  assertThrows(() => parseStudy({ ...study, lines: [study.lines[0], study.lines[0]] }));
  assertThrows(() => parseStudy({ ...study, music: { audioReference: null, excerptStartMs: 10, excerptEndMs: 20 } }));
  assertThrows(() => parseStudy({ ...study, music: { audioReference: "private-file", excerptStartMs: 20, excerptEndMs: 10 } }));
});
