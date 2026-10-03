# Jikanle Android Roadmap

## Goal G1: Validate An Instrumented Android Beta Before Setting A Production Date

Updated 2026-09-09. The mid-August target is retired. Three concrete milestones and team responsibilities: `docs/beta-backlog.md`. The founder has a Play Console account; signing, device QA and live backend verification remain gates.

### Project P1: Bootstrap And Internal Testing Track

- [x] Week 1: Recover existing Android project and keep `:app:assembleDebug` green.
- [x] Week 1: Add product, brand, architecture, contribution, security, ADR, launch, and event docs.
- [x] Week 1: Add CI, release, and Linear PR-reference workflows.
- [x] Week 1: Add the Fuyu no Hanashi seed lesson as a bundled fallback.
- [x] Week 1: Push the bootstrap history to `main`.
- [x] Week 1: Confirm bootstrap CI on `main` at `29b08e8`; the new beta commit needs its own CI run.
- [ ] Week 1: Upload the first signed `.aab` to Play Console internal testing.

### Project P1b: Direct APK Track (Pre-Play, Available Now)

- [x] Week 1: Make `versionCode`/`versionName` env-driven so rebuilds register as updates.
- [x] Week 1: Emit a signed `.apk` alongside the `.aab` and optionally publish a GitHub Release.
- [x] Week 1: Document emulator, phone, and update-loop setup in `docs/beta-testing.md`.
- [ ] Week 1: Configure the four `SIGNING_*` repository secrets.
- [ ] Week 1: Dispatch the release workflow once and install the APK on the founder's phone.

### Project P2: Closed Testing With 12 Attendees From Casa Alternativa Event

- [ ] Week 2: Import closed-test emails from Luma attendees.
- [ ] Week 2: Invite at least 12 opted-in testers.
- [ ] Week 2: Add a Room #0 post-event continuity screen.
- [ ] Week 3: Collect usability notes from testers.
- [ ] Week 3: Fix crash, auth, and lesson-reader issues before production application.

### Project P3: Production Application And Review

- [ ] Week 4: Complete Play Store listing, screenshots, content rating, and data safety.
- [ ] Week 4: Submit production access application.
- [ ] Week 5: Respond to Play review feedback.
- [ ] Week 5: Release production build when approved.

## Goal G2: 100 Monthly Active Learners By End Of 2026

### Project P4: Event #1-#4 In Bogota

- [ ] Week 6: Seed Lesson Library with four Creator-authored songs.
- [ ] Week 7: Add event-to-room invite flow.
- [ ] Week 8: Add post-event lesson reminders without streaks or gamification.
- [ ] Week 9: Publish event recap handoff for web and Android.

### Project P5: Companion Matching MVP

- [ ] Week 10: Design companion profile fields around language, availability, and music taste.
- [ ] Week 11: Add admin-reviewed match suggestions.
- [ ] Week 12: Add private Room entry for matched pairs.
- [ ] Week 13: Measure whether attendees continue conversations after events.

### Project P6: First Paid Lesson Experiment

- [ ] Week 14: Identify one paid Creator-authored Lesson.
- [ ] Week 15: Define entitlement contract with web and backend.
- [ ] Week 16: Build read-only paid Lesson preview.
- [ ] Week 17: Decide whether Android purchase handling belongs in this repo or waits for web checkout.

## Next Session

### Research And Event Pilot (2026-10-02)

- [x] Add Portuguese study preview, validated line ordering and debug-only private JSON import without changing the visual identity.
- [x] Add text-only Portuguese translation contract; reject missing/reordered model output rather than claiming musical accuracy.
- [x] Implement offline event candidate normalization, review cadence helpers, consent-aware connection suggestions and seven-day metric reference tests.
- [x] Draft department boundaries, event-data contract and partner onboarding procedure in ADR 0004 and `docs/events-data-and-discovery.md`.
- [x] Add local ffprobe audio intake with synthetic-WAV tests; copy the general operating plan and event guide to business Docs and Drive.
- [x] Confirm team ownership including Alejandro/David Daza for pedagogy and Robert's cross-disciplinary audit role.
- [ ] Review pending purpose-specific rights manifests and generation enforcement; not cleared for production.
- [ ] Receive permitted Fukahi audio/timestamps; human JA-PT review and musical annotation before cover/video claims.
- [ ] Reconcile event preferences/consents with Web, validate SQL and RLS in staging; nothing applied to production.
- [x] Implement event preference/withdrawal UI and verified host attendance importer; existing click telemetry is not attendance.
- [x] Validate migration twice and owner/host RLS, withdrawal and regrant behavior in disposable Postgres; no production deployment.
- [x] Add MIR reading laboratory, team review protocol, source reference and tested alignment evaluator; no inferred Fukahi timestamps.
- [ ] Recover missing Web `website_v02.sql`, validate combined staging contract, approve consent text and deletion/retention operations.
- [ ] Test preferences on a phone with two staging accounts; review small-screen and enlarged-text layout.
- [ ] Receive founder's public institution/account list and permission evidence; then enable one source transport and review queue.
- [ ] Run debug study import on a phone, then complete signed Play internal testing gates.
- [ ] Review `content_rights.sql` with Web/backend/legal and run two-identity RLS tests before any staging deployment.
- [ ] Recruit first public-domain or directly licensed partner song; record analysis, cover, sync and training permissions separately.
- [ ] Finish Lint in CI or on a less memory-constrained run; local debug build and unit tests passed, but the prolonged Lint run was interrupted.

- Complete `docs/beta-first-install.md`: signing secrets, beta SQL, founder email enrollment and the physical-device checklist.
- Verify `docs/beta-contract.md` with two Supabase identities, then inspect `docs/beta-metrics.sql`.
- Treat the older week labels below as historical sequencing, not calendar deadlines; current acceptance criteria live in `docs/beta-backlog.md`.

- Confirm GitHub Actions on `main` after the latest push.
- Run on the physical phone and check `adb logcat` for a clean first launch.
- Give the Songbridge route a polished navigation entry point if it remains in the MVP.
- Replace downloadable font families with bundled `res/font/` files when `jikanle/brand` provides licensed font assets.
- Move Supabase migrations and seed SQL into `jikanle/db` once that repository is ready.
