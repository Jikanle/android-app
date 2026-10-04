# Fukahi work session: Japanese to Brazilian Portuguese

Updated 2026-10-03. This is a private, human-reviewed cover experiment, not a
published lesson or a validated translation model. The source and derived audio
remain in ignored `research-local/`; do not commit them or send them to a provider
by default. The founder has authorized working with the local file, including
external-provider experiments, but the intake tool does not establish publication,
adaptation, master, synchronization or model-training rights.

## Prepared material

The source is the founder's local SymaG MP3. Its container reports 4:09.626;
decoded audio ends at **4:09.601**. The requested 4:13 endpoint cannot be reached
from this copy. These are founder-supplied approximate regions, not lyric-aligned
ground truth:

| Region | Source clock | File | Purpose |
|---|---|---|---|
| Instrumental build | 2:09–2:27 | `instrumental_build.wav` | Entry, tension, hand-shot lead-in |
| Hand chorus | 2:27–3:00 | `hand_chorus.wav` | Main hand gesture and first high chorus |
| Full hand sequence | 2:09–3:00 | `full_hand_sequence.wav` | Resolve rough cut, transition into chorus |
| Finale | 3:00–4:09.601 | `finale.wav` | Second chorus through source end |

All paths above are under `research-local/fukahi_working/`. `full_source.wav`
retains the **source-file clock**; each cropped WAV starts at zero. For example,
12.4 s in `hand_chorus.wav` is 159.4 s (2:39.4) on the source clock. Use source
milliseconds in `annotations.csv`, even when annotating a crop. `manifest.json`
records the source hash and requested versus effective endpoints; `regions.csv`
and `markers.csv` are the shared handoff. `full_hand_waveform.png` and
`hand_spectrogram.png` are visual inspection aids, not measured beat/pitch labels.

Regenerate from a different authorized local recording without overwriting prior
measurements:

```bash
python3 tools/research/prepare_regions.py \
  '/absolute/path/to/authorized-source.mp3' \
  research-local/fukahi-regions.json \
  research-local/fukahi_working_v2
```

Never align an alternate YouTube/edit timecode to this file without checking the
waveform and source hash. An MP3-to-WAV decode does not restore lost fidelity.

## First session in REAPER and Sonic Visualiser

1. Open `full_source.wav` in REAPER. Set the project display to minutes/seconds;
   place project markers at 2:09, 2:27, 3:00 and 4:09.601 from `markers.csv`.
   Work on copies/takes. Keep the original file untouched.
2. In Sonic Visualiser, open the same full WAV and add waveform plus spectrogram
   layers. Zoom to 2:09–3:00. Mark the **heard** start/end of each sung phrase,
   breaths, section break and any clear dynamic change. Record exact source times
   in `annotations.csv`; do not infer syllable onsets from an instrumental transient.
3. Andrés and Santiago independently mark pulse, strong beats, melodic apex,
   phrase ends and energy/harmonic resolution. Compare disagreements by listening.
   Label an uncertain beat or note as uncertain, not as a model failure.
4. Alejandro and Rafa annotate Japanese meaning units, mora boundaries and
   ambiguity for the same phrase IDs. Keep literal gloss separate from a sung
   Portuguese candidate. A native Brazilian Portuguese singer/reviewer must check
   idiom, stress and whether sustained vowels are actually singable.
5. Alejandro and David define one comprehension question and one novel-utterance
   task tied to the reviewed meaning units. Completion of an activity is not a
   learning-outcome claim.
6. Record a dry guide vocal in REAPER for each candidate, keeping takes distinct.
   Render a 48 kHz/24-bit WAV for Resolve if the video project uses 48 kHz audio;
   preserve the 44.1 kHz analysis files and time mapping. The picture editor
   aligns the hand gesture to a verified musical/lyric landmark, not merely to
   the approximate 2:27 region boundary.

Suggested first annotation is the chorus phrase containing the hand image. Use
`music_phrase`, `beat`, `vocal_note`, `ja_mora`, `meaning_unit`, `pt_syllable`,
`pt_stress`, `breath`, `visual_shot` and `pedagogy` tiers. Each row needs exact
source start/end, method (`manual`, `model:<revision>`), reviewer and status.
Keep the raw independent reviews; only a separate adjudicated row becomes reference.

## Joint cover review gate

For each Portuguese candidate, the text and music teams jointly record:

| Dimension | Evidence to inspect | Reviewer |
|---|---|---|
| Meaning/register | Original meaning units, added/lost claims, Brazilian context | Alejandro, Rafa, PT native reviewer |
| Prosody | PT lexical stress versus accented/long/high notes; breath placement | Alejandro, Andrés, Santiago |
| Musical continuity | Melody contour, harmonic arrival, rhythmic attack, dynamic arc | Andrés, Santiago |
| Performance | Intelligibility, natural vowel duration, actual sung take | PT singer + music team |
| Teaching value | Comprehension, usable vocabulary, learner level, recall task | Alejandro, David |
| Provenance | Audio/take hashes, consent and rights scope, model versions | Alejandro, Robert |

Do not reduce these to one opaque song-quality score. Robert audits baselines and
failure cases with the musicians; a text embedding may rank candidates but cannot
certify meaning, melody or pedagogy. Only reviewed outputs become Android lesson
content. The existing Android reader and Supabase remain unchanged by this audio
preparation step.

## Next technical experiment

After the hand phrase has human boundaries, compare original-mix timing with one
separated-vocal adapter on **the same source clock**. Record model name, code
revision, checkpoint provenance and estimated artifacts. Then run one pitch/F0
baseline on the vocal stem and inspect octave errors manually. Keep separation,
alignment, pitch, text analysis, candidate writing and teaching design as versioned
adapters connected by shared segment IDs, not independent production backends.
`MIR-LAB.md` lists the paper baselines and their limits.
