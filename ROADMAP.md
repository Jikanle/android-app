# Jikanle Android Roadmap

## Goal G1: Ship Jikanle Android To Play Store Production By Mid-August 2026

### Project P1: Bootstrap And Internal Testing Track

- [x] Week 1: Recover existing Android project and keep `:app:assembleDebug` green.
- [x] Week 1: Add product, brand, architecture, contribution, security, ADR, launch, and event docs.
- [x] Week 1: Add CI, release, and Linear PR-reference workflows.
- [x] Week 1: Add the Fuyu no Hanashi seed lesson as a bundled fallback.
- [ ] Week 1: Push the bootstrap history to `main`.
- [ ] Week 1: Confirm GitHub Actions is green on `main`.
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

- Run on the physical phone and check `adb logcat` for a clean first launch.
- Give the Songbridge route a navigation entry point.
- Push the completed bootstrap commits to `main` if network credentials are available.
- Replace downloadable font families with bundled `res/font/` files when `jikanle/brand` provides licensed font assets.
- Move Supabase migrations and seed SQL into `jikanle/db` once that repository is ready.
