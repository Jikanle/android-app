# Three Beta Milestones And Team Ownership

Updated 2026-09-09. These are reviewable backlog items, not remotely created Linear/GitHub issues. Keep one app module with feature/data/domain packages. A six-person team can own boundaries without six deployments. The sixth teammate's identity and responsibility remain unassigned.

## M1: Release Candidate

- [x] Android: preserve event work, transactional cache, functional Luma entry and three primary navigation destinations.
- [x] Android: guided Fuyu deck, requested lesson ID, localized fallback, persistent step and explicit completion.
- [x] Android: optional signed-in observation outbox and separately submitted rating. No anonymous tracking or research claims.
- [ ] Backend + Alejandro: deploy shared beta SQL; pass the two-identity acceptance list in `beta-contract.md`.
- [ ] Alejandro: configure upload signing and Supabase CI values; install the signed release through Play internal testing.
- [ ] Rafa + Alejandro (proposed): inspect a compact phone and a larger phone, dark/light, English/Spanish, 200% text, offline and keyboard layouts. Capture defects with app version and reproducible actions.

Done means a real phone can open the event agenda, resume and finish the lesson, sign in, and send one deduplicated feedback observation. A green build alone does not close M1.

## M2: Internal Then Closed Cohort

- [ ] Alejandro: enroll founder + team internally; store email lists only in Play Console or a private Google Group.
- [ ] Team: run install/update, cold start, offline, OAuth and account-switch tests; fix release blockers.
- [ ] Alejandro: recruit 12-15 relevant participants for closed testing after internal blockers close. Invitations/opt-in do not prove engagement.
- [ ] Robert + Alejandro (proposed): review starter/completion cohorts, return dates and missing-data bias with the SQL queries; do not interpret a rating as learning gain.
- [ ] Alejandro: complete listing, privacy/data-safety and account-deletion requirements shown in Play; document actual shipped data flows.

Done means an evidence log records versions, opted-in cohort, observed failures, fixes and participant feedback. Set a production date only after this evidence exists.

## M3: Research-Compatible Learning Iteration

| Boundary | Proposed owner | First deliverable | Acceptance |
|---|---|---|---|
| ES-JA text, meaning, cultural context | Alejandro | One reviewed lesson annotation fixture | Original meaning, explanation language and source provenance are explicit |
| Music input and musicality | Andres + Santiago | Authorized audio fixture, timing baseline and error report | Rights recorded; timing units and confidence defined; failure cases included |
| Model evaluation and mathematics | Robert | Baseline comparison and uncertainty protocol | Reproducible split/metrics; no universal song-quality number |
| Visual interaction and Japanese presentation | Rafa | Readable phrase/timing prototype consuming a fixture | Android never depends on model runtime; test kana/CJK, screen size and text scaling |
| Events and community | Alejandro | Real calendar entry + public community destination | No fabricated attendance, no private invitation tokens in public tables |
| Pedagogical validation | Unassigned specialist/reviewer | Learning objective + reviewed intervention | Product engagement and learning outcomes evaluated separately |

Scientific expertise is complementary; no role assignment implies an existing validated model. Recruit a language/pedagogy reviewer before claiming educational efficacy. The sixth teammate should fill an agreed gap, not be assigned an invented role here.

## Research Boundary Proposal (Not A Deployed API)

Keep logical packages/services for music input, text analysis, learning activities and music output. Extract a repository only when lifecycle, security or ownership requires it. Finance, instruments, multilingual pronunciation, creation and social realtime stay future work.

Research supplies a versioned artifact: `schema_version`, `artifact_id`, `song_id`, `source_language`, `explanation_language`, `learning_objective`, `source_rights`, `provenance` (paper/model/version/reviewer), and ordered `phrases`. Each phrase has stable `id`, `order`, `original_text`, optional `reading`, `translation`, optional `start_ms/end_ms`, confidence and reviewed notes. Missing timing means untimed display, not zero timestamps. Linguistic confidence and musical accuracy are separate dimensions.

Before consuming a new artifact: agree the shared contract with Web/backend; add valid/missing/unsupported-version fixtures; compare against a manual baseline; obtain creator/pedagogical review; deploy behind an explicit content version. Android maps the artifact to existing lesson/activity models. No paper citation is treated as validated implementation. Reading/listening are first; speaking, writing and song creation require separately reviewed activities and data collection.
