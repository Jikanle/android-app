# AI Agent Handoff

Last updated: 2026-09-08.

## Product Target

Build the fastest useful Android MVP: a clean first-launch lesson experience around Fuyu no Hanashi, plus a seasonal song showcase that can point to commercial music without embedding copyrighted lyrics or audio.

## Current Architecture

- Android package: `co.com.jikanle`.
- UI: Jetpack Compose + Material 3.
- State: ViewModels with Flow.
- Data: repository interfaces under `core/domain/repository`.
- Local cache: Room.
- Remote MVP backend: Supabase.
- Future AI/audio backend: FastAPI job service, GCP-ready, not visible in this MVP.

## Commands

```bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
./gradlew ktlintCheck detekt lint test assembleDebug
```

If `JAVA_HOME` is missing:

```bash
export JAVA_HOME="$HOME/.local/share/JetBrains/Toolbox/apps/android-studio/jbr"
```

## Do Not Touch

- `local.properties`
- Keystores, `.jks`, signing material, OAuth secrets
- Locked brand palette in `BRANDING.md`
- Full copyrighted lyrics or audio
- App package/namespace `co.com.jikanle`

## Open Decisions

- Whether the first seasonal commercial song is Stray Kids or another artist depends on event timing and content clearance.
- Whether FastAPI lives in this repo under `backend/` or a sibling repo should be decided when AI/audio jobs become active.
- Whether closed testing starts immediately or after one more direct phone test depends on Play Console readiness.

## Next Implementation Task

Run the app on a physical device, inspect first-launch UI, verify dark mode, and capture Play Store screenshots for the lesson intro and vocabulary panels.
