"""Prepare private, sample-accurate WAV regions with source-relative markers.

The source and output directory must stay outside version control. This tool does
not infer beats, lyrics, rights, or musical quality.
"""

import argparse
import csv
import hashlib
import json
import math
from pathlib import Path
import re
import subprocess


SAMPLE_RATE = 44100
TIERS = (
    "music_phrase", "beat", "vocal_note", "ja_mora", "meaning_unit",
    "pt_syllable", "pt_stress", "breath", "visual_shot", "pedagogy",
)


def probe_duration_ms(source: Path) -> int:
    result = subprocess.run(
        ["ffprobe", "-v", "error", "-select_streams", "a:0", "-show_entries",
         "stream=codec_name:format=duration", "-of", "json", str(source)],
        check=True, capture_output=True, text=True, timeout=30,
    )
    data = json.loads(result.stdout)
    if not data.get("streams"):
        raise ValueError("Source has no audio stream")
    duration = float(data["format"]["duration"])
    if not math.isfinite(duration) or duration <= 0:
        raise ValueError("Source duration is invalid")
    return round(duration * 1000)


def validate_regions(regions: list[dict], duration_ms: int) -> list[dict]:
    names = set()
    validated = []
    for region in regions:
        name = region["name"]
        if not re.fullmatch(r"[a-z][a-z0-9_-]*", name) or name in names:
            raise ValueError("Region names must be unique lowercase slugs")
        names.add(name)
        start, requested_end = region["start_ms"], region["end_ms"]
        if not isinstance(start, int) or not isinstance(requested_end, int):
            raise ValueError("Region times must be integer milliseconds")
        end = min(requested_end, duration_ms)
        if start < 0 or end <= start:
            raise ValueError(f"Region {name} is outside source duration")
        if requested_end > duration_ms and region.get("end_overrun") != "clamp":
            raise ValueError(f"Region {name} overruns source; opt in to clamping")
        validated.append({
            "name": name, "start_ms": start, "end_ms": end,
            "requested_end_ms": requested_end,
            "clamped": requested_end > duration_ms,
        })
    if not validated:
        raise ValueError("At least one region is required")
    return validated


def decode_wav(source: Path, destination: Path, start_ms: int | None = None,
               end_ms: int | None = None) -> None:
    command = ["ffmpeg", "-hide_banner", "-loglevel", "error", "-nostdin", "-i", str(source)]
    if start_ms is not None and end_ms is not None:
        # Seek after input so MP3 frames are decoded before the requested boundary.
        command += ["-ss", f"{start_ms / 1000:.3f}", "-t", f"{(end_ms - start_ms) / 1000:.3f}"]
    command += ["-map", "0:a:0", "-vn", "-ar", str(SAMPLE_RATE),
                "-c:a", "pcm_s24le", "-y", str(destination)]
    subprocess.run(command, check=True, timeout=300)


def prepare(source: Path, regions_file: Path, output: Path) -> dict:
    source = source.resolve(strict=True)
    if not source.is_file():
        raise ValueError("Source must be a local audio file")
    container_duration_ms = probe_duration_ms(source)
    requested = json.loads(regions_file.read_text(encoding="utf-8"))["regions"]
    validate_regions(requested, container_duration_ms)
    if output.exists():
        raise FileExistsError(f"Output exists: {output}")

    digest = hashlib.sha256()
    with source.open("rb") as audio:
        for block in iter(lambda: audio.read(1024 * 1024), b""):
            digest.update(block)

    output.mkdir(parents=True)
    decode_wav(source, output / "full_source.wav")
    duration_ms = probe_duration_ms(output / "full_source.wav")
    regions = validate_regions(requested, duration_ms)
    for region in regions:
        decode_wav(source, output / f'{region["name"]}.wav', region["start_ms"], region["end_ms"])

    manifest = {
        "schema_version": 1,
        "source_path": str(source),
        "source_sha256": digest.hexdigest(),
        "source_container_duration_ms": container_duration_ms,
        "source_duration_ms": duration_ms,
        "time_origin": "source_file_start",
        "working_format": "44100 Hz stereo-or-source-channels PCM 24-bit WAV",
        "rights_status": "not_verified_by_tool",
        "regions": regions,
    }
    (output / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    with (output / "regions.csv").open("x", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(file, fieldnames=("name", "start_ms", "end_ms", "requested_end_ms", "clamped"))
        writer.writeheader()
        writer.writerows(regions)
    markers = [(region["start_ms"], f'{region["name"]}:start', "founder_requested_region")
               for region in regions]
    markers += [(region["end_ms"], f'{region["name"]}:end',
                 "decoded_source_end_clamp" if region["clamped"] else "founder_requested_region")
                for region in regions]
    with (output / "markers.csv").open("x", newline="", encoding="utf-8") as file:
        writer = csv.writer(file)
        writer.writerow(("source_time_ms", "marker", "provenance"))
        for stamp, label, provenance in sorted(set(markers)):
            writer.writerow((stamp, label, provenance))
    with (output / "annotations.csv").open("x", newline="", encoding="utf-8") as file:
        writer = csv.writer(file)
        writer.writerow(("region", "tier", "source_start_ms", "source_end_ms",
                         "value", "reviewer", "method", "status"))
        for region in regions:
            for tier in TIERS:
                writer.writerow((region["name"], tier, "", "", "", "", "", "pending"))
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("regions_file", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    manifest = prepare(args.source, args.regions_file, args.output)
    print(f'Prepared {len(manifest["regions"])} regions at {args.output}')
    for region in manifest["regions"]:
        if region["clamped"]:
            print(f'{region["name"]}: end clamped from {region["requested_end_ms"]} to {region["end_ms"]} ms')


if __name__ == "__main__":
    main()
