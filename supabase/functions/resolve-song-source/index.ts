import { ResolverError, resolveSongSource } from "./resolver.ts";

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

async function persistSource(source: Record<string, unknown>): Promise<void> {
  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!supabaseUrl || !serviceRoleKey) {
    throw new Error("Supabase runtime credentials unavailable");
  }

  const response = await fetch(
    `${supabaseUrl}/rest/v1/song_sources?on_conflict=provider%2Csource_id`,
    {
      method: "POST",
      headers: {
        apikey: serviceRoleKey,
        Authorization: `Bearer ${serviceRoleKey}`,
        "Content-Type": "application/json",
        Prefer: "resolution=merge-duplicates,return=minimal",
      },
      body: JSON.stringify({ ...source, updated_at: new Date().toISOString() }),
    },
  );
  if (!response.ok) {
    const detail = (await response.text()).slice(0, 300);
    throw new Error(
      `song_sources upsert failed (${response.status}): ${detail}`,
    );
  }
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response(null, { status: 204, headers: corsHeaders(request) });
  }
  if (request.method !== "POST") {
    return json(request, { error: "method_not_allowed" }, 405);
  }

  const contentLength = Number(request.headers.get("content-length") ?? "0");
  if (contentLength > 8_192) {
    return json(request, { error: "request_too_large" }, 413);
  }

  try {
    const input = await request.json();
    const directAudioHosts =
      (Deno.env.get("DIRECT_AUDIO_ALLOWED_HOSTS") ?? "media.jikanle.com.co")
        .split(",").map((host) => host.trim()).filter(Boolean);
    const source = await resolveSongSource(input, { directAudioHosts });
    try {
      await persistSource(source as unknown as Record<string, unknown>);
    } catch (storageError) {
      console.error("song source persistence failed", {
        provider: source.provider,
        source_id: source.source_id,
        message: storageError instanceof Error
          ? storageError.message
          : "unknown error",
      });
      source.warnings.push(
        "Source metadata was resolved but could not yet be stored; retry after applying the song_sources migration.",
      );
    }
    return json(request, source, 200);
  } catch (error) {
    if (error instanceof ResolverError) {
      return json(request, { error: error.message }, error.status);
    }
    if (error instanceof SyntaxError) {
      return json(request, { error: "invalid_json" }, 400);
    }
    return json(request, { error: "source_storage_failed" }, 502);
  }
});
