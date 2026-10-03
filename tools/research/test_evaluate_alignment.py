import copy
import unittest
from evaluate_alignment import evaluate


class AlignmentTest(unittest.TestCase):
    def setUp(self):
        self.reference = {"schema_version": 1, "audio_sha256": "a" * 64,
                          "unit": "word", "time_origin": "source_file_start", "segments": [
                              {"id": "a", "start_ms": 100, "end_ms": 200},
                              {"id": "b", "start_ms": 300, "end_ms": 500}]}

    def test_known_errors_and_missing_coverage(self):
        prediction = copy.deepcopy(self.reference)
        prediction["segments"] = [{"id": "a", "start_ms": 150, "end_ms": 250}]
        result = evaluate(self.reference, prediction)
        self.assertEqual(result["onset_mae_ms"], 50)
        self.assertEqual(result["coverage"], 0.5)
        self.assertEqual(result["missing_ids"], ["b"])
        self.assertEqual(result["both_boundaries_within_tolerance_fraction_of_reference"], 0.5)

    def test_mismatched_audio_duplicates_and_invalid_timing(self):
        for patch in [{"audio_sha256": "b" * 64}, {"unit": "mora"},
                      {"segments": [self.reference["segments"][0]] * 2},
                      {"segments": [{"id": "a", "start_ms": float("nan"), "end_ms": 5}]},
                      {"segments": [{"id": "a", "start_ms": True, "end_ms": 5}]}]:
            with self.assertRaises(ValueError):
                evaluate(self.reference, {**self.reference, **patch})

    def test_no_shared_ids_does_not_report_zero_error(self):
        prediction = copy.deepcopy(self.reference)
        prediction["segments"] = [{"id": "unrelated", "start_ms": 100, "end_ms": 200}]
        result = evaluate(self.reference, prediction)
        self.assertIsNone(result["onset_mae_ms"])
        self.assertEqual(result["coverage"], 0)
