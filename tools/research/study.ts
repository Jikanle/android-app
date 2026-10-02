export interface Study {
  schemaVersion: 1;
  id: string;
  title: string;
  artist: string;
  sourceLanguage: "ja";
  targetLanguage: "pt";
  provenanceUrl: string;
  permission: "private_text_review";
  lines: { id: string; original: string; reading?: string; draft: string; note: string }[];
  music: { audioReference: string | null; excerptStartMs: number | null; excerptEndMs: number | null };
}

/** Narrow pilot contract, not a permission grant, quality score or general content importer. */
export function parseStudy(input: unknown): Study {
  if (!input || typeof input !== "object") throw new Error("expected a study object");
  const s = input as Study;
  if (s.schemaVersion !== 1 || s.sourceLanguage !== "ja" || s.targetLanguage !== "pt" || s.permission !== "private_text_review") throw new Error("unsupported study contract");
  for (const field of [s.id, s.title, s.artist, s.provenanceUrl]) {
    if (typeof field !== "string" || !field.trim() || field.length > 1500) throw new Error("invalid study identity");
  }
  if (new URL(s.provenanceUrl).protocol !== "https:") throw new Error("HTTPS provenance required");
  if (!Array.isArray(s.lines) || s.lines.length < 1 || s.lines.length > 40) throw new Error("study needs 1-40 excerpt lines");
  const ids = new Set<string>();
  for (const line of s.lines) {
    if (!line || typeof line !== "object") throw new Error("invalid line");
    for (const value of [line.id, line.original, line.draft, line.note]) {
      if (typeof value !== "string" || !value.trim() || value.length > 2000) throw new Error("invalid line field");
    }
    if (line.reading != null && (typeof line.reading !== "string" || line.reading.length > 2000)) throw new Error("invalid reading");
    if (ids.has(line.id)) throw new Error("duplicate line identity");
    ids.add(line.id);
  }
  if (!s.music || typeof s.music !== "object") throw new Error("music status is required");
  const { audioReference, excerptStartMs: start, excerptEndMs: end } = s.music;
  if (audioReference !== null && (typeof audioReference !== "string" || !audioReference.trim())) throw new Error("invalid audio reference");
  if (start !== null || end !== null) {
    if (!audioReference || !Number.isSafeInteger(start) || !Number.isSafeInteger(end) || start === null || end === null || start < 0 || end <= start) throw new Error("invalid audio interval");
  }
  return s;
}

export function studyPreview(s: Study) {
  // Always a draft: supplying timestamps is not musical or linguistic validation.
  return {
    id: s.id, titleOriginal: s.title, artist: s.artist, sourceLanguage: s.sourceLanguage,
    lyrics: s.lines.map((line, lineIndex) => ({ lineIndex, text: line.original, transliteration: line.reading })),
    translations: [{ targetLanguage: s.targetLanguage,
      alignmentNote: "Estudo privado. Tradução provisória; pronúncia, ritmo e adaptação cantada ainda não avaliados.",
      lines: s.lines.map((line, lineIndex) => ({ lineIndex, text: line.draft, note: line.note })), vocabulary: [] }],
    vocabulary: [],
  };
}
