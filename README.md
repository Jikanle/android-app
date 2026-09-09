# 乐 Jikanle

**Time that counts twice.**

[![Android CI](https://github.com/Jikanle/android-app/actions/workflows/android-ci.yml/badge.svg)](https://github.com/Jikanle/android-app/actions/workflows/android-ci.yml)
![minSdk 26](https://img.shields.io/badge/minSdk-26-6D2DD3)
![Kotlin 2.2.10](https://img.shields.io/badge/Kotlin-2.2.10-3B5BDB)
![License BSL 1.1](https://img.shields.io/badge/license-BSL%201.1-E63B96)

Jikanle is a language-learning brand built on a simple belief: most hours people spend trying to learn a language are also hours they spend alone, and that is the bug, not the user. Music is the glue between learning and belonging. Jikanle starts as small in-person evenings in Bogota, then extends online so the bond formed in the room continues at home.

## Sibling Repositories

- `Jikanle/android-app` — this single Gradle app module, organized by Kotlin packages.
- `Jikanle/Jikanle-Website` — shared Web client and event publishing surface.
- `Jikanle/backend` — shared platform ownership; the business workspace exposes the current Android `supabase/` SQL under `backend/supabase/`.
- `Jikanle/cultural-translation-research` — research repository identified by the founder; local experiments also live under `ml_models/songbridge/` in the business workspace.

The business workspace aggregates projects and is not necessarily one Git repository. No new Music-Input, Music-output, Events, brand or database repositories are needed for this beta. See [team ownership](docs/beta-backlog.md).

## Beta Status

Current: public events and Luma entry, guided Fuyu lesson with local progress, optional authenticated usage metrics and queued rating feedback. Audio opens an external Spotify search. Android does not play, record or score audio. Songbridge remains a development route.

Not yet verified: live auth, deployed beta SQL/RLS, physical-device UI or signed Play upload. Play Console account exists per founder. Start with [the first-install guide](docs/beta-first-install.md) and [the data contract](docs/beta-contract.md).

Actual quality checks are Kotlin compilation, Android Lint and JUnit tests. `ktlintCheck` and `detekt` are currently empty placeholder tasks.

## Getting Started

1. Clone the repository.
2. Copy `local.properties.example` to `local.properties`.
3. Fill the Supabase URL and anon key. Use the public anon key only.
4. Open the project in Android Studio or run:

```bash
./gradlew :app:assembleDebug
```

If `JAVA_HOME` is missing, use the Android Studio bundled JDK:

```bash
export JAVA_HOME="$HOME/.local/share/JetBrains/Toolbox/apps/android-studio/jbr"
./gradlew :app:assembleDebug
```

## Project Structure

```text
app/                 Android application module.
app/src/main/java/   Compose UI, ViewModels, data repositories, Room cache, Hilt modules.
data/seed/           Bundled Lesson JSON used only as the first demo fallback.
supabase/            Shared schema/functions, exposed through the business backend workspace.
docs/decisions/      Architecture Decision Records.
docs/                Play Store, event, and session handoff docs.
build-logic/         Reserved for Gradle convention plugins as the repo modularizes.
```

## For Humans Joining The Project

Read `CLAUDE.md` for product context, `ARCHITECTURE.md` before touching feature/data boundaries, `CONTRIBUTING.md` for workflow, and `BRANDING.md` before changing UI.

Contact: `alesanchezpov@gmail.com`  
Events: https://luma.com/Jikanle?k=c
