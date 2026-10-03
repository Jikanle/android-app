"""Evaluate manually referenced timing, not transcription, translation or learning quality."""
import argparse
import json
import math
from pathlib import Path
import re
from statistics import mean, median


def validate(data):
    if not isinstance(data, dict) or data.get("schema_version") != 1:
        raise ValueError("Expected alignment schema_version 1")
    if not isinstance(data.get("audio_sha256"), str) or not re.fullmatch(r"[a-f0-9]{64}", data["audio_sha256"]):
        raise ValueError("Audio hash required")
    if data.get("time_origin") != "source_file_start" or data.get("unit") not in ("line", "word", "mora", "phoneme"):
        raise ValueError("Declare source-file milliseconds and annotation unit")
    segments = data.get("segments")
    if not isinstance(segments, list) or not segments:
        raise ValueError("Non-empty segments required")
    result = {}
    previous = -1
    for segment in segments:
        if not isinstance(segment, dict):
            raise ValueError("Expected segment object")
        identity = segment.get("id")
        start, end = segment.get("start_ms"), segment.get("end_ms")
        if not isinstance(identity, str) or not identity.strip() or identity in result:
            raise ValueError("Unique stable segment IDs required")
        if any(type(t) not in (int, float) or not math.isfinite(t) for t in (start, end)):
            raise ValueError("Finite numeric times required")
        if start < 0 or end <= start or start < previous:
            raise ValueError("Positive durations and ordered starts required")
        result[identity] = (start, end)
        previous = start
    return result


def evaluate(reference, prediction, tolerance_ms=100):
    if type(tolerance_ms) not in (int, float) or not math.isfinite(tolerance_ms) or tolerance_ms < 0:
        raise ValueError("Invalid tolerance")
    ref, pred = validate(reference), validate(prediction)
    for field in ("audio_sha256", "unit", "time_origin"):
        if reference[field] != prediction[field]:
            raise ValueError(f"Cannot compare different {field}")
    common = [key for key in ref if key in pred]
    onset = [abs(ref[k][0] - pred[k][0]) for k in common]
    offset = [abs(ref[k][1] - pred[k][1]) for k in common]
    hits = sum(a <= tolerance_ms and b <= tolerance_ms for a, b in zip(onset, offset))
    return {
        "schema_version": 1, "unit": reference["unit"], "audio_sha256": reference["audio_sha256"],
        "reference_segments": len(ref), "matched_segments": len(common),
        "coverage": len(common) / len(ref), "missing_ids": sorted(ref.keys() - pred.keys()),
        "extra_ids": sorted(pred.keys() - ref.keys()),
        "onset_mae_ms": mean(onset) if onset else None,
        "onset_median_ms": median(onset) if onset else None,
        "offset_mae_ms": mean(offset) if offset else None,
        "tolerance_ms": tolerance_ms,
        "both_boundaries_within_tolerance_fraction_of_reference": hits / len(ref),
        "scope": "timing_only_not_cover_quality",
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("reference", type=Path)
    parser.add_argument("prediction", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--tolerance-ms", type=float, default=100)
    args = parser.parse_args()
    result = evaluate(json.loads(args.reference.read_text()), json.loads(args.prediction.read_text()), args.tolerance_ms)
    with args.output.open("x", encoding="utf-8") as output:
        json.dump(result, output, indent=2, allow_nan=False)
    print("Timing report created. Missing segments count against coverage; no quality certification.")


if __name__ == "__main__":
    main()
