# JA-PT Pilot: Fukahi

Updated 2026-10-02. The official anime site identifies **不可避 / Fukahi**, performed
by 島爺, as the ending theme. This corrects the working spelling "Fukashi".
[Official announcement](https://ragnarok-official.com/1st/news/94/).

## What Runs Now

Android Lesson -> Japanese and Portuguese opens the existing branded comparator.
The bundled Sakura example remains the public demo. A debug-only file picker accepts
a local study export, validates complete ordered line coverage, and displays original,
translation and reviewer notes. Portuguese draft glossary is available for Sakura.
No recording, licensed track, waveforms, pronunciation score or synchronized player
is included. Imports stay in memory; after process death reopen the file. Invalid
imports show the retry state; retry returns to the bundled demo.

The founder-provided short romanized fragment is in ignored
`research-local/fukahi-study.json`, not seed SQL or a committed public catalog.
Its Portuguese wording is a **provisional meaning gloss**, not an approved cover.
No missing lyrics or timestamps were retrieved or invented.

```bash
deno run --allow-read=research-local --allow-write=research-local \
  tools/research/run-study.ts research-local/fukahi-study.json \
  research-local/fukahi-android.json
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb push research-local/fukahi-android.json /sdcard/Download/
```

On the phone: Lesson -> Japanese and Portuguese -> Open local study -> choose
`fukahi-android.json`. Verify source, Portuguese draft and uncertainty note; rotate,
change languages on the Sakura fallback and test airplane mode. The picker is absent
from release builds. The local file contains no audio and grants no publication rights.

## Research Handoff

Every artifact must include study/line IDs, schema version, source version or checksum,
tool/model version, reviewer, date, method, units, confidence/limitations and rights
scope. Unknown measurements remain null, never zero or a fabricated score. Keep
candidate translations side by side; do not overwrite the human baseline.

| Owner (proposed) | Deliverable | Acceptance |
|---|---|---|
| Alejandro + Rafa (JA), Alejandro (PT) | Meaning, narrative, deixis, register, ambiguity, draft alternatives | Check the excerpt in context; record tradeoffs and literal vs sung version separately |
| Andres | Authorized original excerpt annotation | Phrase/beat/tension landmarks with measured times and uncertainty; no assumed one-note-per-syllable mapping |
| Santiago | Portuguese performed take | Same comparison interval, declared key/tempo changes; assess stresses, breath, vowels, diction and mix with a rubric |
| Alejandro, supported/audited by Robert | Cover/text/model experiments and reproducible evaluation | Manual baseline, annotator disagreement, alignment MAE where ground truth exists; Robert challenges metrics with Andres/Santiago |
| Rafa | Visual timing and Android inspection | Hand gesture matched to an approved landmark, readable captions, phone captures and no layout overlap |
| Alejandro + David Daza | One teacher-authored activity and evaluation | Retrieval with feedback plus contextual production; immediate and seven-day check; no claim of efficacy from a tiny beta |

Team areas confirmed by Alejandro on 2026-10-02; individual acceptance/hours pending.
No invitations were sent or Linear issues created. Robert audits AI methodology and
asks Andres/Santiago to validate musical interpretations rather than replacing their judgment.

## Music Review

Separate semantic fidelity, prosody, performance and engineering. For each candidate
record preserved/changed imagery, lexical stresses vs musical accents, mora/syllable
segmentation, sustained vowels, consonant attacks, breaths, phrase timing and climaxes.
Then compare relative dynamics, register, arrangement, spectral balance and acoustic
space. Use level-matched authorized takes and human listening, not loudness as quality.

The earlier "95-98% quantization", mandatory open vowels, exact mix replication and
one note per syllable are not universal acceptance rules. They are testable artistic
choices that depend on singer, genre, ornamentation and intended adaptation. A sung
melody also cannot establish correct spoken Japanese pitch accent. Text-unit counts
are estimates, not duration or singability. The translation prompt now states this.

## Pedagogy And Brazilian Framing

Start with a specific objective: understand and use the fragment's remaining/presence
idea in a new context. Show meaning, ask a learner to explain an analogous memory in
the target language, provide human feedback, then test recall/transfer later. Do not
infer a complete CEFR level from one song or equate event attendance with learning.
Choose related vocabulary in a second authored activity only after reviewer approval.

Keep the video a Brazil-Japan cultural encounter unless the founder explicitly
chooses otherwise. Shot plan: original permitted Brazil/event image -> hand detail at
the annotated musical landmark -> JA/PT text comparison -> actual app screen -> public
event invitation. Timing remains pending audio. Do not imply partner endorsement or
assert election manipulation without evidence. Permissions for people/photos are
separate from music permissions.

## Rights And Future Models

Track distinct grants for source/master use, translation/adaptation, synchronization,
distribution, performer likeness, research and model training, with territory, duration,
withdrawal terms and evidence. A private text test is not a grant to publish a cover.
Original composition and recording rights may have different holders; revenue splits
require agreement, not an automatic percentage invented by this app.
[WIPO music value chain](https://www.wipo.int/en/web/ipday/2016/creating_value_from_music).

Use opt-in training and traceable datasets; keep non-training content out of training
exports. Fairly Trained describes licensing-based eligibility, not certification of
Jikanle or a guarantee of output rights. Start hosting a small authorized catalog with
access control and usage reporting before considering broader music distribution.
[Fairly Trained criteria](https://www.fairlytrained.org/certifications).

Aimedic may share evaluation methods first, not participant audio. Healthcare use
needs a separate purpose, consent, governance and domain-specific validation. Song
recognition does not establish clinical speech accuracy.

## Two-Week Delivery

Week 1: founder supplies permitted excerpt/times; language reviewer approves one gloss;
Andres/Santiago annotate/perform one interval; Rafa captures the local Android test;
Robert freezes the manual baseline; pedagogy owner defines one measurable objective.

Week 2: compare candidate takes, perform a small usability session, record failures
and delayed recall separately, approve launch assets/rights, and promote only reviewed
content through the shared backend. Keep JA-ES as the main language roadmap; JA-PT is
this pilot. KO-ES, KO-PT, ZH-ES and ZH-PT remain planned, not implemented.
