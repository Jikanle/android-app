# 乐 Jikanle

**Time that counts twice.**

[![Android CI](https://github.com/Jikanle/android-app/actions/workflows/android-ci.yml/badge.svg)](https://github.com/Jikanle/android-app/actions/workflows/android-ci.yml)
![minSdk 26](https://img.shields.io/badge/minSdk-26-6D2DD3)
![Kotlin 2.2.10](https://img.shields.io/badge/Kotlin-2.2.10-3B5BDB)
![License BSL 1.1](https://img.shields.io/badge/license-BSL%201.1-E63B96)

Jikanle is a language-learning brand built on a simple belief: most hours people spend trying to learn a language are also hours they spend alone, and that is the bug, not the user. Music is the glue between learning and belonging. Jikanle starts as small in-person evenings in Bogota, then extends online so the bond formed in the room continues at home.

## Sibling Repositories

- `jikanle/web-app` — Next.js 14 and Supabase web client. Canonical backend contracts live there first.
- `jikanle/brand` — logos, palette tokens, and typography files. Until it exists, this repo mirrors the locked palette in `BRANDING.md` and `core/design`.
- `jikanle/db` — Supabase migrations, RLS policies, and seed data. Android consumes the schema; it does not own it.
- `jikanle/lesson-content` — structured Lesson JSON and LRC files. Until it exists, `data/seed/fuyu_no_hanashi.json` is the bundled lesson seed.
- `jikanle/research` — LAMIR paper and MIR experiments for the long-term aligner.
- `jikanle/docs` — private founder notes, briefs, and playbooks.

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
supabase/            Current shared schema/function drafts; canonical migrations move to jikanle/db.
docs/decisions/      Architecture Decision Records.
docs/                Play Store, event, and session handoff docs.
build-logic/         Reserved for Gradle convention plugins as the repo modularizes.
```

## For Humans Joining The Project

Read `CLAUDE.md` for product context, `ARCHITECTURE.md` before touching feature/data boundaries, `CONTRIBUTING.md` for workflow, and `BRANDING.md` before changing UI.

Contact: `alesanchezpov@gmail.com`  
Events: https://luma.com/Jikanle?k=c
