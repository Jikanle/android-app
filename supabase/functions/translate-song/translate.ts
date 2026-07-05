// Singable, emotion-preserving translation via Anthropic (Claude).
// Ported from ml_models/songbridge/songbridge/translate.py so the Edge Function and
// the local CLI stay in lockstep. This is Node 2 (the "moat"): we host it, not a
// literal-MT provider — DeepL/Google give meaning but not meter or emotion.

export const LANGS = ["ja", "en", "es", "zh"] as const;
export type Lang = typeof LANGS[number];

const UNIT_BY_LANG: Record<Lang, string> = {
  ja: "morae (kana ≈ 1 mora; long vowels, ん, っ each count)",
  zh: "hanzi (1 character = 1 syllable)",
  es: "syllables (apply synalepha across word boundaries)",
  en: "syllables",
};
const LANG_NAME: Record<Lang, string> = {
  ja: "Japanese",
  zh: "Mandarin Chinese",
  es: "Spanish",
  en: "English",
};

export interface TranslatedLine {
  source: string;
  target: string;
  source_units: number;
  target_units: number;
  emotion: string;
  stressed: string;
  note: string;
}

export function isLang(x: unknown): x is Lang {
  return typeof x === "string" && (LANGS as readonly string[]).includes(x);
}

const SYSTEM =
  `You translate song lyrics so they can be SUNG over the original melody in another
language. You are not writing a literal gloss — you are re-performing the line so a
native speaker of the target language feels what a native speaker of the source feels.

Two hard constraints, in priority order:
1. MEANING & EMOTIONAL IMPACT — preserve the core image and the feeling. The target
   line must land with the same emotional weight, keeping emphasis on the idea the
   melody emphasizes.
2. UNIT BUDGET — each note carries ~1 unit, so match the source's per-line unit count
   within +/- 1 unit. Put the important words on the strong beats.
Then, with any freedom left: prefer open vowels where the melody sustains, keep natural
phrasing, rhyme only if it costs nothing.

Never pad with filler that breaks meaning. Being off by one unit but emotionally true is
the right call. Return ONLY valid JSON. No markdown fences, no commentary.`;

function buildPrompt(lines: string[], src: Lang, tgt: Lang): string {
  const numbered = lines
    .map((l, i) => `${i + 1}. ${l}`)
    .join("\n");
  return `Source language: ${LANG_NAME[src]} — counting unit: ${UNIT_BY_LANG[src]}
Target language: ${LANG_NAME[tgt]} — counting unit: ${UNIT_BY_LANG[tgt]}

For each line: (a) read the source and identify the feeling and the word the melody
leans on; (b) write a SINGABLE ${LANG_NAME[tgt]} line that carries that feeling and
matches the source unit count within +/- 1; (c) count units in source and translation
honestly.

Lines:
${numbered}

Return JSON exactly:
{"lines": [
  {"source": "<source line>",
   "target": "<singable ${LANG_NAME[tgt]} translation>",
   "source_units": <int>, "target_units": <int>,
   "emotion": "<1-2 words: the feeling this line must land>",
   "stressed": "<the ${LANG_NAME[tgt]} words on the strong beats>",
   "note": "<one short clause: a singability choice or tradeoff>"}
]}`;
}

function extractJson(raw: string): string {
  let s = raw.trim().replace(/^```(?:json)?/i, "").replace(/```$/i, "").trim();
  // If the model wrapped JSON in prose, grab the outermost object.
  const start = s.indexOf("{");
  const end = s.lastIndexOf("}");
  if (start > 0 || end < s.length - 1) s = s.slice(start, end + 1);
  return s;
}

/** Call Claude and return the singable translation for one language pair. */
export async function translateLines(
  lines: string[],
  src: Lang,
  tgt: Lang,
  apiKey: string,
  model = "claude-sonnet-4-20250514",
): Promise<TranslatedLine[]> {
  const res = await fetch("https://api.anthropic.com/v1/messages", {
    method: "POST",
    headers: {
      "x-api-key": apiKey,
      "anthropic-version": "2023-06-01",
      "content-type": "application/json",
    },
    body: JSON.stringify({
      model,
      max_tokens: 8000,
      thinking: { type: "adaptive" },
      system: SYSTEM,
      messages: [{ role: "user", content: buildPrompt(lines, src, tgt) }],
    }),
  });
  if (!res.ok) {
    const detail = await res.text();
    throw new Error(`anthropic ${res.status}: ${detail.slice(0, 300)}`);
  }
  const data = await res.json();
  const textBlock = (data.content ?? []).find((b: { type: string }) => b.type === "text");
  if (!textBlock?.text) throw new Error("anthropic returned no text block");
  const parsed = JSON.parse(extractJson(textBlock.text));
  if (!Array.isArray(parsed.lines)) throw new Error("model JSON missing lines[]");
  return parsed.lines as TranslatedLine[];
}
