import { ResolverError, resolveSongSource } from "./resolver.ts";

function assertEquals(actual: unknown, expected: unknown): void {
  if (!Object.is(actual, expected)) {
    throw new Error(
      `Expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`,
    );
  }
}

async function assertResolverError(
  action: () => Promise<unknown>,
  messagePart: string,
): Promise<void> {
  try {
    await action();
  } catch (error) {
    if (error instanceof ResolverError && error.message.includes(messagePart)) {
      return;
    }
    throw error;
  }
  throw new Error("Expected ResolverError");
}

const noMetadata: typeof fetch = () =>
  Promise.resolve(new Response(null, { status: 503 }));

Deno.test("normalizes YouTube watch URL and forbids audio processing", async () => {
  const result = await resolveSongSource(
    { url: "https://www.youtube.com/watch?v=dQw4w9WgXcQ" },
    { fetcher: noMetadata },
  );
  assertEquals(result.provider, "youtube");
  assertEquals(result.source_id, "dQw4w9WgXcQ");
  assertEquals(
    result.embed_url,
    "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ",
  );
  assertEquals(result.audio_url, null);
  assertEquals(result.audio_processing_allowed, false);
});

Deno.test("normalizes Spotify track URL and forbids audio processing", async () => {
  const result = await resolveSongSource(
    { url: "https://open.spotify.com/track/4cOdK2wGLETKBW3PvgPWqT" },
    { fetcher: noMetadata },
  );
  assertEquals(result.provider, "spotify");
  assertEquals(result.source_id, "4cOdK2wGLETKBW3PvgPWqT");
  assertEquals(
    result.embed_url,
    "https://open.spotify.com/embed/track/4cOdK2wGLETKBW3PvgPWqT",
  );
  assertEquals(result.audio_processing_allowed, false);
});

Deno.test("allows licensed audio only on an approved host", async () => {
  const result = await resolveSongSource({
    url: "https://media.jikanle.com.co/public-domain/sakura.mp3",
    title: "Sakura Sakura",
    artist: "Traditional",
    license: "public-domain",
  }, { directAudioHosts: ["media.jikanle.com.co"] });
  assertEquals(result.provider, "self_hosted");
  assertEquals(result.playback_type, "direct_audio");
  assertEquals(result.audio_processing_allowed, true);
  assertEquals(result.audio_url, result.external_url);
});

Deno.test("rejects direct audio without an approved license", async () => {
  await assertResolverError(
    () =>
      resolveSongSource({
        url: "https://media.jikanle.com.co/public-domain/sakura.mp3",
        title: "Sakura Sakura",
        artist: "Traditional",
      }, { directAudioHosts: ["media.jikanle.com.co"] }),
    "approved",
  );
});

Deno.test("rejects arbitrary direct-audio hosts", async () => {
  await assertResolverError(
    () =>
      resolveSongSource({
        url: "https://untrusted.example/song.mp3",
        title: "Unknown",
        artist: "Unknown",
        license: "CC0-1.0",
      }, { directAudioHosts: ["media.jikanle.com.co"] }),
    "unsupported",
  );
});
