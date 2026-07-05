// generate-song — Node 4. Re-sing a translation with ElevenLabs Music, store the
// audio in the private song-renders bucket, and record it per user.
//
// POST body: { "translation_id": "<uuid>" }
// Secrets (Supabase Edge Function secrets): ELEVENLABS_API_KEY, plus the runtime
// SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY.
//
// Guardrails, because the ElevenLabs key is the owner's and pays for everyone:
//   - auth-gated (only signed-in users trigger it);
//   - HARD global cap of GENERATION_BUDGET takes (refuses beyond it);
//   - dedup: one take per (owner, translation) — re-requests return the stored one.

const allowedOrigins = new Set(
  (Deno.env.get("ALLOWED_ORIGINS") ??
    "https://jikanle.com.co,https://www.jikanle.com.co")
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
const BUCKET = "song-renders";
const GENERATION_BUDGET = 5; // hard cap — protects the owner's ElevenLabs spend
const MUSIC_LENGTH_MS = 30_000; // 30s clip keeps each take cheap
const LANG_NAME: Record<string, string> = {
  ja: "Japanese",
  zh: "Chinese",
  es: "Spanish",
  en: "English",
};

export function buildMusicPrompt(
  targetLanguage: string,
  lines: Array<{ translated_text: string; emotion: string | null }>,
): string {
  const emotions = [
    ...new Set(lines.map((line) => line.emotion).filter(Boolean)),
  ]
    .slice(0, 4).join(", ");
  const language = LANG_NAME[targetLanguage] ?? targetLanguage;
  return `Original ${language} song with a distinct new melody. Voice-forward, emotionally ${
    emotions || "tender"
  }. ` +
    `Do not imitate any existing artist, recording, or composition. Sing only these user-provided adapted lines:\n` +
    lines.map((line) => line.translated_text).join("\n");
}

function svc(extra: Record<string, string> = {}): Record<string, string> {
  return {
    apikey: SERVICE_ROLE!,
    Authorization: `Bearer ${SERVICE_ROLE}`,
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
async function getUserId(authHeader: string | null): Promise<string | null> {
  if (!authHeader) return null;
  const res = await fetch(`${SUPABASE_URL}/auth/v1/user`, {
    headers: { apikey: SERVICE_ROLE!, Authorization: authHeader },
  });
  if (!res.ok) return null;
  const u = await res.json();
  return typeof u?.id === "string" ? u.id : null;
}
async function signUrl(
  path: string,
  expiresIn = 604_800,
): Promise<string | null> {
  const res = await fetch(
    `${SUPABASE_URL}/storage/v1/object/sign/${BUCKET}/${path}`,
    {
      method: "POST",
      headers: svc({ "Content-Type": "application/json" }),
      body: JSON.stringify({ expiresIn }),
    },
  );
  if (!res.ok) return null;
  const { signedURL } = await res.json();
  return signedURL ? `${SUPABASE_URL}/storage/v1${signedURL}` : null;
}

export async function handler(request: Request): Promise<Response> {
  if (request.method === "OPTIONS") {
    return new Response(null, { status: 204, headers: corsHeaders(request) });
  }
  if (request.method !== "POST") {
    return json(request, { error: "method_not_allowed" }, 405);
  }
  if (!SUPABASE_URL || !SERVICE_ROLE) {
    return json(request, { error: "server_misconfigured" }, 500);
  }

  const elevenKey = Deno.env.get("ELEVENLABS_API_KEY");
  if (!elevenKey) {
    return json(request, { error: "ELEVENLABS_API_KEY not set" }, 500);
  }

  const ownerId = await getUserId(request.headers.get("Authorization"));
  if (!ownerId) return json(request, { error: "unauthorized" }, 401);

  let input: Record<string, unknown>;
  try {
    input = await request.json();
  } catch {
    return json(request, { error: "invalid_json" }, 400);
  }
  const translationId = input.translation_id;
  if (typeof translationId !== "string") {
    return json(request, { error: "translation_id required" }, 400);
  }

  try {
    // 1. Dedup — return the existing take for this (owner, translation).
    const existing = await (await db(
      `song_generations?owner_id=eq.${ownerId}&translation_id=eq.${translationId}&select=id,audio_path,duration_ms`,
      { method: "GET", headers: svc() },
    )).json() as Array<{ id: string; audio_path: string; duration_ms: number }>;
    if (existing.length > 0) {
      const audioUrl = await signUrl(existing[0].audio_path);
      return json(request, {
        generation_id: existing[0].id,
        audio_url: audioUrl,
        duration_ms: existing[0].duration_ms,
        reused: true,
      });
    }

    // 2. Hard global budget.
    const all = await (await db(`song_generations?select=id`, {
      method: "GET",
      headers: svc(),
    })).json() as unknown[];
    if (all.length >= GENERATION_BUDGET) {
      return json(request, {
        error: "generation_budget_exhausted",
        budget: GENERATION_BUDGET,
        used: all.length,
      }, 429);
    }

    // 3. Load the translation + its lines (our own translated text — never the source copyright).
    const tr = await (await db(
      `song_translations?id=eq.${translationId}&select=id,song_id,target_language`,
      { method: "GET", headers: svc() },
    )).json() as Array<
      { id: string; song_id: string; target_language: string }
    >;
    if (tr.length === 0) {
      return json(request, { error: "translation_not_found" }, 404);
    }
    const { song_id, target_language } = tr[0];

    const lines = await (await db(
      `song_translation_lines?translation_id=eq.${translationId}&order=line_index.asc&select=translated_text,emotion`,
      { method: "GET", headers: svc() },
    )).json() as Array<{ translated_text: string; emotion: string | null }>;
    if (lines.length === 0) {
      return json(request, { error: "no_translation_lines" }, 422);
    }

    const prompt = buildMusicPrompt(target_language, lines);

    // 4. ElevenLabs Music → MP3 bytes.
    const music = await fetch("https://api.elevenlabs.io/v1/music", {
      method: "POST",
      headers: { "xi-api-key": elevenKey, "Content-Type": "application/json" },
      body: JSON.stringify({
        prompt,
        model_id: "music_v2",
        music_length_ms: MUSIC_LENGTH_MS,
      }),
    });
    if (!music.ok) {
      return json(request, {
        error: "elevenlabs_failed",
        detail: (await music.text()).slice(0, 300),
      }, 502);
    }
    const audio = new Uint8Array(await music.arrayBuffer());

    // 5. Store in the private bucket under the owner's folder.
    const path = `${ownerId}/${translationId}.mp3`;
    const up = await fetch(
      `${SUPABASE_URL}/storage/v1/object/${BUCKET}/${path}`,
      {
        method: "POST",
        headers: svc({ "Content-Type": "audio/mpeg", "x-upsert": "true" }),
        body: audio,
      },
    );
    if (!up.ok) {
      return json(request, {
        error: "storage_upload_failed",
        detail: (await up.text()).slice(0, 200),
      }, 502);
    }

    // 6. Record it + return a signed URL.
    const row = await (await db("song_generations", {
      method: "POST",
      headers: svc({
        "Content-Type": "application/json",
        Prefer: "return=representation",
      }),
      body: JSON.stringify({
        owner_id: ownerId,
        song_id,
        translation_id: translationId,
        target_language,
        provider: "elevenlabs",
        prompt,
        audio_path: path,
        duration_ms: MUSIC_LENGTH_MS,
      }),
    })).json() as Array<{ id: string }>;

    const audioUrl = await signUrl(path);
    return json(request, {
      generation_id: row[0].id,
      audio_url: audioUrl,
      duration_ms: MUSIC_LENGTH_MS,
      reused: false,
    });
  } catch (e) {
    return json(
      request,
      { error: "generation_failed", detail: String(e).slice(0, 300) },
      502,
    );
  }
}

if (import.meta.main) Deno.serve(handler);
