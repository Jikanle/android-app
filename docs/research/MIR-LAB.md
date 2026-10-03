# Jikanle MIR and joint cover laboratory

Updated 2026-10-03. A research workspace for Japanese -> Brazilian Portuguese
covers that teach Japanese to Portuguese speakers. This is not a trained Jikanle
cover model, nor a claim that a complete commercial pipeline is already working.

## MVP boundary

1. Community: public agenda, external RSVP, optional event preferences, host-verified
   attendance and consent-aware engagement queries. No attendance inferred from clicks.
2. Music: one creator-reviewed learning module, ordered JA/PT text, vocabulary,
   explanations and review notes. Private Fukahi experiment; public seed remains
   separate. Input audio, measured alignment and a reviewed Portuguese performance
   are required before calling it a synchronized cover demo.

No rooms/chat, automatic song recommendations, voice cloning, automatic pedagogy,
training corpus collection or streaming catalog in this MVP. Keep one Supabase
system of record and package/adaptor boundaries before creating more services/repos.

## What runs versus what is proposed

| Stage | Existing runnable component | Limit / next work |
|---|---|---|
| Source reference | `resolve-song-source` metadata/embed contract; `tools/research/fukahi-source.json` | No provider audio download; link not verified by browsing |
| Authorized local intake | `inspect_audio.py`: ffprobe, SHA-256, duration, interval | No pitch, transcription or permission certification |
| Text draft | `translate-song/translate.ts`, ordered JA/PT contract; private study exporter | Prompt-based emotion/unit estimates, not independently measured musical constraints |
| Legacy research | Python Songbridge translate/syllable modules and `sense_score.py` | Approximate text counts; PT parity and local model execution still need review |
| Timing evaluation | `evaluate_alignment.py` + synthetic tests | Compares supplied annotations; does NOT generate timestamps |
| Music analysis | Separation/F0/beat/alignment adapters below | Not installed or integrated in this session |
| Cover synthesis | Existing `generate-song` code requests a DISTINCT new melody | Not a melody-preserving cover engine; no live generation validated |
| Joint review | Protocol and handoff below | Human review now; automated constrained candidate search later |
| Android | Songbridge text/glossary preview + private debug JSON importer | Audio-aligned research contract not yet consumed by player |

## The full-song reference and timestamps

Founder supplied [this YouTube reference](https://www.youtube.com/watch?v=qmq5JVh8xhE).
The normalized video ID is recorded without playlist/tracking parameters. Fetching
the page failed here, so no title, duration or exact phrase timestamp was inferred.
Provider playback time is not a downloaded waveform and different edits can shift it.

For immediate inspection, use official playback to note approximate phrase start/end;
the [IFrame API](https://developers.google.com/youtube/iframe_api_reference) exposes
player time/seek controls, not raw audio for MIR. For reproducible analysis supply an
authorized local file or your own recording. Record its SHA-256 and file-relative
milliseconds; do not mix a radio/live/TV edit with full-song timings.

Workflow: reference -> permitted recording -> exact fragment -> human annotation ->
analysis adapter -> reviewed PT candidate -> own/authorized performance -> independent
evaluation -> lesson/video. No assumed timestamps, no copied master in the repo.

## Shared representation and team boundaries

Use stable `study_id`, `line_id`, `candidate_id`, source/candidate audio hashes,
`schema_version`, model/checkpoint revision, run ID and reviewer for every artifact.
Keep measured data, inferred hypotheses and human judgments visibly distinct.

| Contract / owner | Input -> output | Acceptance |
|---|---|---|
| Linguistics: Alejandro + Rafa (JA), Alejandro (PT) | Reviewed JA text -> tokens/readings, mora boundaries, meaning units, emotion hypotheses, rhetorical emphasis, PT candidates | Reviewer identifies ambiguity, meaning lost/added, stress and register; no embedding-only approval |
| Music input: Andres + Santiago | Authorized waveform -> stems, F0/voicing, note/beat landmarks, phrase boundaries, breaths, tension annotations | Time origin/hash/units recorded, errors auditioned; automatic confidence not treated as truth |
| Joint cover: Alejandro + musicians, Robert audits | BOTH above -> revised text-to-note/mora/syllable mapping, authorized deviations, candidate performance | Bilingual semantic review AND sung naturalness/musical review; no one-dimensional score |
| Pedagogy: Alejandro + David Daza | Reviewed segment + learner baseline -> objective, activity, comprehension/recall rubric | Pilot tests comprehension/vocabulary first, production next, delayed recall separately |
| Model/evaluation: Alejandro + Robert | Versioned runs -> comparison, failure analysis, compute report | Baselines, held-out songs/singers, uncertainty and ablations; consult musicians on musical failure |
| Interaction: Rafa | Approved outputs -> readable comparison, playback controls and review feedback | Preserves identity; user understands original versus adaptation |

Suggested artifact groups: `text-analysis.json`, `music-analysis.json`,
`cover-candidates.json`, `reviews.json`, `learning-protocol.md`. These names are
research handoffs, NOT newly deployed Supabase tables. Training, Web and Android
consume reviewed results; Android must not implement tokenization or audio models.
Each adapter must accept a versioned input and emit provenance plus outputs; unknown
fields/units need deliberate migration. Missing music must mean `not_evaluated`,
never a fabricated singability score. Expensive workers may later queue GPU jobs;
do not put long-running synthesis into a synchronous mobile request.

## Papers: selected strong venues and recent candidates

This is a curated starting set of **eight** papers, not a systematic count of the
entire field or a universal SOTA ranking. Compare on the same benchmark, split,
training data budget, language and metric. A 2026 preprint is not automatically
better than a reproducible older baseline. None establishes JA/PT learning efficacy.

| Paper / status | Why read it for Jikanle | Exercise / limit |
|---|---|---|
| [Hybrid Transformers for Music Source Separation](https://arxiv.org/abs/2211.08553), Rouard et al., ICASSP 2023 | HT Demucs separation baseline; waveform/spectral attention | Compare original mix and extracted vocal for alignment, retaining artifacts; published extra-training-data result is not a guarantee on our song |
| [Mel-RoFormer for Vocal Separation and Vocal Melody Transcription](https://archives.ismir.net/ismir2024/paper/000049.pdf), Wang et al., ISMIR 2024 | Links source separation with melody transcription | Explain which task labels/metrics change; do not mistake similarly named community checkpoints for the paper implementation |
| [Improving Lyrics-to-Audio Alignment Using Frame-wise Phoneme Labels with Masked Cross Entropy Loss](https://www.dafx.de/paper-archive/2025/DAFx25_paper_15.pdf), Cheng et al., DAFx 2025 | Timing of the phrase and hand gesture; partial boundary supervision | Reports Jamendo onset MAE 216 ms and median 41 ms. Its CMU phoneme inventory is not Japanese; adaptation needs language-specific phonemization and evaluation |
| [CREPE: A Convolutional Representation for Pitch Estimation](https://arxiv.org/abs/1802.06182), Kim et al., ICASSP 2018 | Reproducible monophonic F0 baseline, not current universal SOTA | Detect octave errors and unvoiced frames; full mix is not a clean singing stem. [Authors' code](https://github.com/marl/crepe) |
| [CLAP: Learning Audio Concepts From Natural Language Supervision](https://arxiv.org/abs/2206.04769), Elizalde et al., ICASSP 2023 | Audio/text retrieval and representation baseline | Same mood can accompany wrong lyrics. Similarity cannot certify semantic fidelity, melodic fidelity or learning |
| [Do Audio-Language Models Understand Linguistic Variations?](https://aclanthology.org/2025.naacl-short.76/), Selvakumar et al., NAACL 2025 | Tests robustness to textual query variations | Paraphrase emotion descriptions, rerank fixed candidates, report instability; no training required for this diagnostic |
| [TCSinger 2](https://aclanthology.org/2025.findings-acl.687/), Zhang et al., Findings ACL 2025 | Controlled multilingual singing synthesis; content/style representations | Distinguish synthesis from conversion and TTS. Verify checkpoint language coverage, training provenance, compute and license before JA/PT integration |
| [YingMusic-Singer](https://arxiv.org/abs/2603.24589), Hao et al., 2026 preprint | Melody-preserving lyric manipulation is closer to our cover task than generic music prompting | [Official repository](https://github.com/ASLP-lab/YingMusic-Singer-Plus) advertises Chinese/English, not JA/PT; CC BY code/weights with a separately licensed Stability component. Study the method, do not promise Portuguese output |

No weights were downloaded or models trained. Reproduction requires the exact paper
version, code commit, checkpoint, dataset rights, measured peak VRAM and runtime.
Two 4090s are a resource, not evidence that any chosen model fits or that upstream
training data meets Jikanle's opt-in policy. Prefer one independently reproducible
job per GPU before multi-GPU complexity.

## First Fukahi experiment

1. Alejandro supplies a permitted WAV/FLAC/MP3 plus the phrase interval. Preserve
   original file; verify whether it matches the referenced full-song edit.
2. Alejandro/Rafa establish Japanese text/readings and stable segment IDs. Romanized
   spaces alone do not define Japanese lexical, phoneme or mora boundaries.
3. Andres/Santiago annotate onset/offset independently in Sonic Visualiser or Praat.
   Resolve disagreements, retaining both raw annotations and adjudicated reference.
4. Compare manual equal-duration baseline with one pinned alignment adapter, on
   original mix and separated vocal. The uniform baseline is a control, not true sync.
5. Alejandro drafts literal PT gloss plus two singable candidates; musicians mark
   breaths, note placement and deliberate rhythmic changes. Preserve references
   back to JA meaning units even when syllable counts differ.
6. Record own PT take; evaluate meaning and singing separately. If no permitted
   vocal exists, use a fresh consenting singer or clearly label a spoken guide.
7. David/Alejandro create one human-authored comprehension prompt and a novel-sentence
   use task. Delayed recall at 24h/7d is research, not retention telemetry. No efficacy
   claim from one song or convenience sample; agree consent and study design first.

```bash
python3 tools/research/inspect_audio.py research-local/original.wav \
  research-local/audio-manifest.json --start-ms START_MS --end-ms END_MS
python3 tools/research/evaluate_alignment.py research-local/reference.json \
  research-local/predicted.json research-local/timing-report.json --tolerance-ms 100
python3 -m unittest discover -s tools/research -p 'test_*.py'
```

Reference/prediction shape (synthetic example, not Fukahi timing):

```json
{"schema_version":1,"audio_sha256":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
 "unit":"word","time_origin":"source_file_start",
 "segments":[{"id":"line-01-word-01","start_ms":100,"end_ms":250}]}
```

Replace synthetic hashes/times with measurements. Both files must reference the
same audio hash/unit; separated-stem outputs must be mapped back to the source
timeline before comparison. Missing segments penalize coverage. Output gives onset
MAE/median, offset MAE and fraction of all reference segments whose two boundaries
meet a chosen tolerance. The default 100 ms is an experimental setting, not a
validated product threshold. This is not the identical protocol of every paper.
Use established `mir_eval` metrics for later melody/separation evaluation instead
of inventing a new universal music-quality score.

## Study questions and review guide

| Question to answer before running models | Minimum defensible answer |
|---|---|
| Why can median timing error be small while MAE is large? | A few badly aligned words; inspect error distribution and missing coverage |
| Does a better separated vocal always improve alignment? | No; removed consonants and artifacts can worsen timing; use an ablation |
| Is one mora one syllable or one note? | No; distinguish language units, note events and melisma; manually label mapping |
| Does a high F0 similarity establish good pronunciation? | No; consonants, vowels, duration and intelligibility need separate evidence |
| Does small CLAP distance prove a faithful translation? | No; test same-mood/wrong-meaning negatives and bilingual review |
| How should we split a future cover dataset? | Group by composition and performer to limit leakage; test unseen language pairs separately |
| Can every strong syllable be forced onto beats 1 and 3? | No; musical meter/phrase/syncopation and linguistic stress require contextual review |
| Does giving MIDI 95-98 percent precision guarantee human feel? | No universal rule; define an audible target and compare measured/artistic timing |
| Are closed vowels always wrong on high notes? | No absolute ban; singer, register and vowel modification matter; test with musicians |
| Does a completed lesson prove vocabulary learning? | No; use independently scored baseline/post/delayed/transfer measures |

Suggested first week: day 1 intake + annotation; day 2 separation/timing comparison;
day 3 text/music candidate review; day 4 own performance and blind comparison;
day 5 one teaching module and failure review. Second week: consented usability pilot,
delayed measurements, stabilize one adapter, document failures before scaling.

## Fair contribution space

Accept covers of varied skill levels, label recording conditions/quality rather than
excluding beginners automatically. Keep original audio, derived stems, lyrics,
adaptations, performer permission and training grant provenance separate. The
performer of a cover cannot necessarily license the underlying composition/lyrics.
Training opt-in is explicit, optional, purpose/model scoped and not bundled with
event attendance or playback. Record withdrawal and dataset versions; explain limits
on undoing already completed training rather than promising automatic unlearning.

A short fragment is not automatically unrestricted use; applicable exceptions and
licenses depend on jurisdiction/use. Review with qualified counsel before public
distribution or training. [Copyright Office FAQ](https://www.copyright.gov/help/faq/faq-fairuse.html).
Fairly Trained's criteria require appropriate training rights and due diligence on
upstream models; alignment with that goal is not certification. [Criteria](https://www.fairlytrained.org/certifications),
[FAQ](https://www.fairlytrained.org/faqs). No participant audio is transferred to
healthcare projects or another company under these event/research permissions.
