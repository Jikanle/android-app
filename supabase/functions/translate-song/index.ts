// translate-song — Node 2. Given a song + source lyric lines, produce a singable
// translation (Claude) and persist song_translations + song_translation_lines.
//
// POST body:
//   { "song_id": "<uuid>", "source_language": "ja", "target_language": "es",
//     "lines": ["...optional source lines..."] }
// If "lines" is omitted, existing public.song_lyric_lines for source_language are used.
//
// Secrets (Supabase → Edge Function secrets): ANTHROPIC_API_KEY, plus the
// runtime-provided SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY.
//
// Auth: requires a signed-in user (Authorization: Bearer <jwt>) so anonymous callers
// cannot spend Anthropic tokens. Writes use the service role (bypasses RLS); per-user
// ownership + dedupe is a follow-up schema change (see NODES.md / STATUS.md).

import {
  isLang,
  type Lang,
  type TranslatedLine,
  translateLines,
} from "./translate.ts";

const allowedOrigins = new Set(
  (Deno.env.get("ALLOWED_ORIGINS") ??
    "https://jikanle.com.co,https://www.jikanle.com.co,https://jikanle-website.vercel.app,https://jikanle-website-alejandrosanchezpoveda-asperjasp.vercel.app")
    .split(",").map((origin) => origin.trim()).filter(Boolean),
);
function corsHeaders(request: Request): Record<string, string> {
  const origin = request.headers.get("Origin") ?? "";
  return {
    "Access-Control-Allow-Origin": allowedOrigins.has(origin)
      ? origin
      : "https://www.jikanle.com.co",
    "Access-Control-Allow-Headers":
      "authorization, apikey, content-type, x-client-info",
    "Access-Control-Allow-Methods": "POST, OPTIONS",
    "Vary": "Origin",
  };
}

function json(request: Request, body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders(request),
      "Content-Type": "application/json",
      "Cache-Control": "no-store",
    },
  });
}

const SUPABASE_URL = Deno.env.get("SUPABASE_URL");
const SERVICE_ROLE = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

function dbHeaders(extra: Record<string, string> = {}): Record<string, string> {
  return {
    apikey: SERVICE_ROLE!,
    Authorization: `Bearer ${SERVICE_ROLE}`,
    "Content-Type": "application/json",
    ...extra,
  };
}

async function db(path: string, init: RequestInit): Promise<Response> {
  const res = await fetch(`${SUPABASE_URL}/rest/v1/${path}`, init);
  if (!res.ok) {
    throw new Error(
      `db ${init.method} ${path} -> ${res.status}: ${
        (await res.text()).slice(0, 200)
      }`,
    );
  }
  return res;
}

async function verifyUser(authHeader: string | null): Promise<boolean> {
  if (!authHeader) return false;
  const res = await fetch(`${SUPABASE_URL}/auth/v1/user`, {
    headers: { apikey: SERVICE_ROLE!, Authorization: authHeader },
  });
  return res.ok;
}

async function getSourceLines(songId: string, src: Lang): Promise<string[]> {
  const res = await db(
    `song_lyric_lines?song_id=eq.${songId}&language=eq.${src}&order=line_index.asc&select=line_index,text`,
    { method: "GET", headers: dbHeaders() },
  );
  const rows = (await res.json()) as Array<
    { line_index: number; text: string }
  >;
  return rows.map((r) => r.text);
}

async function upsertSourceLines(
  songId: string,
  src: Lang,
  lines: string[],
): Promise<void> {
  const payload = lines.map((text, line_index) => ({
    song_id: songId,
    line_index,
    language: src,
    text,
  }));
  await db("song_lyric_lines?on_conflict=song_id,language,line_index", {
    method: "POST",
    headers: dbHeaders({
      Prefer: "resolution=merge-duplicates,return=minimal",
    }),
    body: JSON.stringify(payload),
  });
}

async function createSong(
  meta: Record<string, unknown>,
  language: Lang,
): Promise<string> {
  const title = String(meta.title_original ?? "").trim();
  const artist = String(meta.artist ?? "").trim();
  if (!title || !artist) {
    throw new Error("song.title_original and song.artist are required");
  }
  const res = await db("songs", {
    method: "POST",
    headers: dbHeaders({ Prefer: "return=representation" }),
    body: JSON.stringify({
      title_original: title,
      title_romanized: meta.title_romanized
        ? String(meta.title_romanized)
        : null,
      artist,
      language,
      is_public_domain: false,
    }),
  });
  const rows = (await res.json()) as Array<{ id: string }>;
  return rows[0].id;
}

async function upsertTranslation(
  songId: string,
  src: Lang,
  tgt: Lang,
  report: string,
): Promise<string> {
  const res = await db(
    "song_translations?on_conflict=song_id,source_language,target_language",
    {
      method: "POST",
      headers: dbHeaders({
        Prefer: "resolution=merge-duplicates,return=representation",
      }),
      body: JSON.stringify({
        song_id: songId,
        source_language: src,
        target_language: tgt,
        provider: "anthropic",
        alignment_report: report,
      }),
    },
  );
  const rows = (await res.json()) as Array<{ id: string }>;
  return rows[0].id;
}

async function replaceTranslationLines(
  translationId: string,
  lines: TranslatedLine[],
): Promise<void> {
  await db(`song_translation_lines?translation_id=eq.${translationId}`, {
    method: "DELETE",
    headers: dbHeaders(),
  });
  const payload = lines.map((l, line_index) => ({
    translation_id: translationId,
    line_index,
    translated_text: l.target,
    source_units: l.source_units ?? null,
    target_units: l.target_units ?? null,
    emotion: l.emotion ?? null,
    stressed: l.stressed ?? null,
    singability_note: l.note ?? null,
  }));
  await db("song_translation_lines", {
    method: "POST",
    headers: dbHeaders({ Prefer: "return=minimal" }),
    body: JSON.stringify(payload),
  });
}

function buildReport(lines: TranslatedLine[], src: Lang, tgt: Lang): string {
  let within = 0;
  const rows = lines.map((l) => {
    const d = (l.target_units ?? 0) - (l.source_units ?? 0);
    if (Math.abs(d) <= 1) within += 1;
    return `${l.source_units} | ${l.target_units} | ${
      d >= 0 ? "+" : ""
    }${d} | ${l.emotion} | ${l.target}`;
  });
  const n = lines.length || 1;
  return `# ${src}->${tgt}\nSRC | TGT | Δ | EMOTION | TARGET\n${
    rows.join("\n")
  }\n` +
    `|Δ|<=1 on ${within}/${n} (${Math.floor((100 * within) / n)}%)`;
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response(null, { status: 204, headers: corsHeaders(request) });
  }
  if (request.method !== "POST") {
    return json(request, { error: "method_not_allowed" }, 405);
  }
  if (!SUPABASE_URL || !SERVICE_ROLE) {
    return json(request, { error: "server_misconfigured" }, 500);
  }

  const anthropicKey = Deno.env.get("ANTHROPIC_API_KEY");
  if (!anthropicKey) {
    return json(request, { error: "ANTHROPIC_API_KEY not set" }, 500);
  }

  if (Number(request.headers.get("content-length") ?? "0") > 32_768) {
    return json(request, { error: "request_too_large" }, 413);
  }
  if (!(await verifyUser(request.headers.get("Authorization")))) {
    return json(request, { error: "unauthorized" }, 401);
  }

  let input: Record<string, unknown>;
  try {
    input = await request.json();
  } catch {
    return json(request, { error: "invalid_json" }, 400);
  }

  const src = input.source_language;
  const tgt = input.target_language;
  if (!isLang(src) || !isLang(tgt)) {
    return json(request, { error: "language must be ja|en|es|zh" }, 400);
  }
  if (src === tgt) {
    return json(request, { error: "source and target must differ" }, 400);
  }

  try {
    // Use an existing song, or create one from provided metadata (paste-link flow).
    let songId: string;
    if (typeof input.song_id === "string") {
      songId = input.song_id;
    } else if (input.song && typeof input.song === "object") {
      songId = await createSong(
        input.song as Record<string, unknown>,
        src as Lang,
      );
    } else {
      return json(request, {
        error: "provide song_id, or song:{title_original,artist}",
      }, 400);
    }

    let sourceLines: string[];
    if (Array.isArray(input.lines) && input.lines.length > 0) {
      sourceLines = (input.lines as unknown[]).map(String).filter((l) =>
        l.trim()
      );
      await upsertSourceLines(songId, src, sourceLines);
    } else {
      sourceLines = await getSourceLines(songId, src);
    }
    if (sourceLines.length === 0) {
      return json(request, {
        error:
          "no source lyric lines (provide `lines` or seed song_lyric_lines)",
      }, 422);
    }

    const translated = await translateLines(
      sourceLines,
      src,
      tgt,
      anthropicKey,
    );
    const report = buildReport(translated, src, tgt);
    const translationId = await upsertTranslation(songId, src, tgt, report);
    await replaceTranslationLines(translationId, translated);

    return json(request, {
      translation_id: translationId,
      song_id: songId,
      source_language: src,
      target_language: tgt,
      line_count: translated.length,
      alignment_report: report,
      lines: translated,
    });
  } catch (e) {
    return json(request, {
      error: "translation_failed",
      detail: String(e).slice(0, 300),
    }, 502);
  }
});
