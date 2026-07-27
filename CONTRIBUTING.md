# Contributing To Jikanle Android

## Workflow

This repo uses trunk-based development. `main` is always shippable. Bootstrap may push directly to `main`; after that, all changes go through pull requests.

Every PR needs at least one self-review or agent-review. Keep PRs small enough to inspect. Do not mix unrelated refactors with product changes.

## Commits

Use conventional commits:

- `feat(lesson): render vocabulary slide`
- `fix(auth): handle expired refresh token`
- `docs: update Play Store launch checklist`
- `ci: require Linear issue references`

## Linear

Team key: `JIK`.

Project structure:

- **Android MVP** — mirrors Goal G1 in `ROADMAP.md`.
- **Lesson Library seed** — mirrors Goal G2 event-led lesson work.

Labels to create:

- `android`
- `web`
- `backend`
- `docs`
- `brand`
- `event`
- `research`

Every PR body must include a Linear issue reference such as `JIK-42` or `Fixes JIK-42`. The `linear-sync.yml` workflow fails PRs without one.

Every session's first task is to check Linear's "In progress" column for handoffs before starting new work.

Do not install any Linear SDK in the Android app. Linear integration is only at GitHub/PR level.

## Code Standards

- No hardcoded user-facing strings. Use `res/values/strings.xml` and `res/values-en/strings.xml`.
- No Supabase calls from Composables.
- Keep product logic in ViewModels, repositories, or domain use cases.
- Preserve stable lyric and slide ordering.
- Do not add flashcards, SRS, streaks, XP, leaderboards, generic chatbots, or social-network chrome.

## Validation

Run:

```bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
```

Run Android instrumented tests only when a device or emulator is available.
