export type SongSourceProvider =
  | "youtube"
  | "spotify"
  | "jamendo"
  | "self_hosted";
export type PlaybackType = "embed" | "direct_audio";

export type ResolveSongSourceInput = {
  url: string;
  title?: string;
  artist?: string;
  license?: string;
};

export type SongSourceResponse = {
  provider: SongSourceProvider;
  source_id: string;
  title: string;
  artist: string;
  thumbnail_url: string | null;
  external_url: string;
  embed_url: string;
  playback_type: PlaybackType;
  audio_url: string | null;
  audio_processing_allowed: boolean;
  license: string | null;
  warnings: string[];
};

export class ResolverError extends Error {
  constructor(message: string, public readonly status = 400) {
    super(message);
  }
}

type ResolverOptions = {
  fetcher?: typeof fetch;
  directAudioHosts?: string[];
};

const YOUTUBE_ID = /^[A-Za-z0-9_-]{11}$/;
const SPOTIFY_ID = /^[A-Za-z0-9]{22}$/;
const DIRECT_AUDIO_EXTENSIONS = /\.(mp3|m4a|ogg|wav)$/i;
const PROCESSABLE_LICENSES = new Map([
  ["public-domain", "public-domain"],
  ["cc0-1.0", "CC0-1.0"],
  ["cc-by-4.0", "CC-BY-4.0"],
  ["cc-by-sa-4.0", "CC-BY-SA-4.0"],
]);

function parseHttpUrl(value: string): URL {
  let url: URL;
  try {
    url = new URL(value);
  } catch {
    throw new ResolverError("url must be a valid absolute URL");
  }
  if (url.protocol !== "https:") {
    throw new ResolverError("only HTTPS song sources are accepted");
  }
  url.hash = "";
  return url;
}

export function parseYouTubeId(url: URL): string | null {
  const host = url.hostname.toLowerCase().replace(/^www\./, "");
  let id: string | null = null;
  if (host === "youtu.be") {
    id = url.pathname.split("/").filter(Boolean)[0] ?? null;
  }
  if (["youtube.com", "m.youtube.com", "music.youtube.com"].includes(host)) {
    id = url.searchParams.get("v");
    if (!id) {
      const parts = url.pathname.split("/").filter(Boolean);
      if (["embed", "shorts", "live"].includes(parts[0])) id = parts[1] ?? null;
    }
  }
  return id && YOUTUBE_ID.test(id) ? id : null;
}

export function parseSpotifyTrackId(url: URL): string | null {
  const host = url.hostname.toLowerCase().replace(/^www\./, "");
  if (host !== "open.spotify.com") return null;
  const parts = url.pathname.split("/").filter(Boolean);
  const trackIndex = parts.indexOf("track");
  const id = trackIndex >= 0 ? parts[trackIndex + 1] : null;
  return id && SPOTIFY_ID.test(id) ? id : null;
}

async function fetchOEmbed(
  endpoint: string,
  fetcher: typeof fetch,
): Promise<Record<string, unknown> | null> {
  try {
    const response = await fetcher(endpoint, {
      headers: { Accept: "application/json" },
      signal: AbortSignal.timeout(3_000),
    });
    if (!response.ok) return null;
    return await response.json() as Record<string, unknown>;
  } catch {
    return null;
  }
}

function text(value: unknown): string | null {
  return typeof value === "string" && value.trim() ? value.trim() : null;
}

export async function resolveSongSource(
  input: ResolveSongSourceInput,
  options: ResolverOptions = {},
): Promise<SongSourceResponse> {
  if (!input || typeof input.url !== "string") {
    throw new ResolverError("request body must include url");
  }

  const url = parseHttpUrl(input.url.trim());
  const fetcher = options.fetcher ?? fetch;
  const youtubeId = parseYouTubeId(url);

  if (youtubeId) {
    const externalUrl = `https://www.youtube.com/watch?v=${youtubeId}`;
    const metadata = await fetchOEmbed(
      `https://www.youtube.com/oembed?url=${
        encodeURIComponent(externalUrl)
      }&format=json`,
      fetcher,
    );
    const metadataMissing = metadata === null;
    return {
      provider: "youtube",
      source_id: youtubeId,
      title: text(metadata?.title) ?? input.title?.trim() ??
        `YouTube video ${youtubeId}`,
      artist: text(metadata?.author_name) ?? input.artist?.trim() ??
        "Unknown artist",
      thumbnail_url: text(metadata?.thumbnail_url) ??
        `https://i.ytimg.com/vi/${youtubeId}/hqdefault.jpg`,
      external_url: externalUrl,
      embed_url: `https://www.youtube-nocookie.com/embed/${youtubeId}`,
      playback_type: "embed",
      audio_url: null,
      audio_processing_allowed: false,
      license: null,
      warnings: [
        "Playback only: downloading or processing YouTube audio is not allowed.",
        ...(metadataMissing
          ? ["YouTube metadata unavailable; fallback metadata was used."]
          : []),
      ],
    };
  }

  const spotifyId = parseSpotifyTrackId(url);
  if (spotifyId) {
    const externalUrl = `https://open.spotify.com/track/${spotifyId}`;
    const metadata = await fetchOEmbed(
      `https://open.spotify.com/oembed?url=${encodeURIComponent(externalUrl)}`,
      fetcher,
    );
    const metadataMissing = metadata === null;
    return {
      provider: "spotify",
      source_id: spotifyId,
      title: text(metadata?.title) ?? input.title?.trim() ??
        `Spotify track ${spotifyId}`,
      artist: text(metadata?.author_name) ?? input.artist?.trim() ??
        "Unknown artist",
      thumbnail_url: text(metadata?.thumbnail_url),
      external_url: externalUrl,
      embed_url: `https://open.spotify.com/embed/track/${spotifyId}`,
      playback_type: "embed",
      audio_url: null,
      audio_processing_allowed: false,
      license: null,
      warnings: [
        "Playback only: downloading or processing Spotify audio is not allowed.",
        ...(metadataMissing
          ? ["Spotify metadata unavailable; fallback metadata was used."]
          : []),
      ],
    };
  }

  const directHosts = options.directAudioHosts ?? [];
  if (
    directHosts.map((host) => host.toLowerCase()).includes(
      url.hostname.toLowerCase(),
    )
  ) {
    if (!DIRECT_AUDIO_EXTENSIONS.test(url.pathname)) {
      throw new ResolverError(
        "direct audio URL must end in mp3, m4a, ogg, or wav",
      );
    }
    const normalizedLicense = PROCESSABLE_LICENSES.get(
      input.license?.trim().toLowerCase() ?? "",
    );
    if (!normalizedLicense) {
      throw new ResolverError(
        "direct audio requires an approved public-domain or Creative Commons license",
      );
    }
    if (!input.title?.trim() || !input.artist?.trim()) {
      throw new ResolverError("direct audio requires title and artist");
    }
    return {
      provider: "self_hosted",
      source_id: url.href,
      title: input.title.trim(),
      artist: input.artist.trim(),
      thumbnail_url: null,
      external_url: url.href,
      embed_url: url.href,
      playback_type: "direct_audio",
      audio_url: url.href,
      audio_processing_allowed: true,
      license: normalizedLicense,
      warnings: [
        "License and source ownership must remain auditable before processing.",
      ],
    };
  }

  throw new ResolverError(
    "unsupported song source; use YouTube, Spotify, or an approved direct-audio host",
    422,
  );
}
