"""Local audio intake only; never download, transcribe, train, or score pronunciation."""

import argparse
import hashlib
import json
import math
from pathlib import Path
import subprocess


def inspect_audio(path: Path, start_ms: int, end_ms: int) -> dict:
    path = path.resolve(strict=True)
    if not path.is_file():
        raise ValueError("Input must be a local audio file")
    if start_ms < 0 or end_ms <= start_ms:
        raise ValueError("Choose a positive interval, in milliseconds")
    result = subprocess.run(
        ["ffprobe", "-v", "error", "-protocol_whitelist", "file,pipe", "-select_streams", "a:0",
         "-show_entries", "stream=codec_name,sample_rate,channels,duration:format=duration",
         "-of", "json", str(path)],
        check=True, capture_output=True, text=True, timeout=30,
    )
    probe = json.loads(result.stdout)
    streams = probe.get("streams", [])
    if not streams:
        raise ValueError("No audio stream found")
    stream = streams[0]
    duration = float(stream.get("duration", probe.get("format", {}).get("duration", "nan")))
    if not math.isfinite(duration) or duration <= 0 or end_ms > duration * 1000:
        raise ValueError("Excerpt is outside the measurable audio duration")
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for block in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(block)
    version = subprocess.run(["ffprobe", "-version"], check=True, capture_output=True, text=True, timeout=10).stdout.splitlines()[0]
    return {
        "schema_version": 1, "source_sha256": digest.hexdigest(), "tool": version,
        "duration_ms": round(duration * 1000), "sample_rate_hz": int(stream["sample_rate"]),
        "channels": int(stream["channels"]), "codec": stream["codec_name"],
        "excerpt_start_ms": start_ms, "excerpt_end_ms": end_ms,
        "time_origin": "source_file_start", "rights_status": "not_verified_by_tool",
        "alignment": None, "pitch": None, "pronunciation_score": None,
        "training_allowed": False, "publication_allowed": False,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("audio", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--start-ms", type=int, required=True)
    parser.add_argument("--end-ms", type=int, required=True)
    args = parser.parse_args()
    if args.audio.resolve() == args.output.resolve():
        parser.error("Output must not replace the source")
    manifest = inspect_audio(args.audio, args.start_ms, args.end_ms)
    # Exclusive creation protects previous measurements and the original recording.
    with args.output.open("x", encoding="utf-8") as output:
        json.dump(manifest, output, indent=2)
    print("Local audio manifest created; musical and language review still pending.")


if __name__ == "__main__":
    main()
