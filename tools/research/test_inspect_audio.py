import tempfile
import unittest
import wave
from pathlib import Path

from inspect_audio import inspect_audio


class AudioIntakeTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.audio = Path(self.directory.name) / "synthetic.wav"
        with wave.open(str(self.audio), "wb") as output:
            output.setnchannels(1)
            output.setsampwidth(2)
            output.setframerate(16000)
            output.writeframes(b"\0\0" * 16000)

    def test_measures_synthetic_audio_without_claiming_quality(self):
        result = inspect_audio(self.audio, 100, 900)
        self.assertEqual(result["duration_ms"], 1000)
        self.assertEqual(result["sample_rate_hz"], 16000)
        self.assertEqual(len(result["source_sha256"]), 64)
        self.assertIsNone(result["alignment"])
        self.assertFalse(result["training_allowed"])

    def test_rejects_outside_and_reversed_intervals(self):
        for start, end in [(0, 1001), (-1, 500), (500, 400)]:
            with self.assertRaises(ValueError):
                inspect_audio(self.audio, start, end)
