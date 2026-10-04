import json
from pathlib import Path
import tempfile
import unittest
import wave

from prepare_regions import prepare, probe_duration_ms, validate_regions


class RegionValidationTest(unittest.TestCase):
    def test_clamps_only_when_explicit(self):
        regions = validate_regions(
            [{"name": "finale", "start_ms": 180000, "end_ms": 253000,
              "end_overrun": "clamp"}], 249626,
        )
        self.assertEqual(regions[0]["end_ms"], 249626)
        self.assertTrue(regions[0]["clamped"])

    def test_rejects_implicit_overrun_and_duplicate_names(self):
        with self.assertRaises(ValueError):
            validate_regions([{"name": "finale", "start_ms": 180000,
                               "end_ms": 253000}], 249626)
        with self.assertRaises(ValueError):
            validate_regions([{"name": "verse", "start_ms": 0, "end_ms": 1000},
                              {"name": "verse", "start_ms": 1000, "end_ms": 2000}], 249626)

    def test_prepares_source_relative_region_without_overwriting(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source.wav"
            with wave.open(str(source), "wb") as output:
                output.setnchannels(1)
                output.setsampwidth(2)
                output.setframerate(16000)
                output.writeframes(b"\0\0" * 32000)
            config = root / "regions.json"
            config.write_text(json.dumps({"regions": [{"name": "clip", "start_ms": 500,
                                                        "end_ms": 1500}]}), encoding="utf-8")
            destination = root / "prepared"
            result = prepare(source, config, destination)
            self.assertEqual(result["source_duration_ms"], 2000)
            self.assertEqual(probe_duration_ms(destination / "clip.wav"), 1000)
            self.assertEqual(result["regions"][0]["start_ms"], 500)
            self.assertIn("500,clip:start", (destination / "markers.csv").read_text())
            with self.assertRaises(FileExistsError):
                prepare(source, config, destination)


if __name__ == "__main__":
    unittest.main()
