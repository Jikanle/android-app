# Jikanle Android — Codex Agent Instructions

## Role and scope

This is the Kotlin/Jetpack Compose Android repository. It is linked into the business root as `android_app/`. Android is an offline-capable client of the shared Supabase backend; it does not own a separate backend or product contract.

`CLAUDE.md` is the canonical product and strategy brief for all coding agents. This file adds Codex/OpenAI-specific operating instructions and repository guardrails.

For the current MVP, implement only enough UI/data flow to fetch or load the demo song and show title, artist, languages, ordered original/translated lines, vocabulary, and useful alignment notes.

## Architecture

Follow the existing structure and dependencies:

- Compose UI and navigation;
- ViewModels for screen state;
- repository/domain boundaries for data access;
- Room as the local cache and Supabase Kotlin as the remote source;
- Hilt for dependency injection;
- one shared kotlinx-serialization configuration for backend models.

Do not call Supabase directly from Composables, place product logic in UI code, hardcode user-facing strings, or create giant one-file screens. Map shared backend DTOs deliberately and preserve stable lyric line ordering.

Bundled JSON is acceptable only as an explicit demo fallback. Do not let it become independent Android-owned product state.

## Supabase and secrets

The URL and anon key come from ignored `local.properties` entries and are injected through `BuildConfig`:

```properties
SUPABASE_URL=...
SUPABASE_ANON_KEY=...
```

Never inspect, print, edit, or commit `local.properties`. Never place a `service_role` key in the app. All client access must be safe under RLS.

## Build and validation

Use the Android Studio bundled JDK if `JAVA_HOME` is unavailable:

```bash
export JAVA_HOME="$HOME/.local/share/JetBrains/Toolbox/apps/android-studio/jbr"
./gradlew :app:assembleDebug
```

Run relevant unit/UI tests when the changed area has coverage. At minimum, build the debug app and provide manual device steps for the demo flow. Do not add a new testing framework solely for the MVP.

If shared DTOs, queries, or schema assumptions change, document the exact fields and add a dated note to the business-root `STATUS.md` naming Web and backend impact.

## Codex-specific hints

- Prefer `rg` / `rg --files` for code search.
- Prefer `./gradlew :app:assembleDebug` and `./gradlew testDebugUnitTest` over broad `gradle build` invocations.
- Treat `local.properties`, keystores, and any OAuth/signing material as unreadable secrets.
- The actual app package is `co.com.jikanle`; do not move it to `co.jikanle` unless a migration is requested.
- Keep bundled JSON as a demo fallback only. Canonical content belongs in `jikanle/lesson-content` and canonical schema in `jikanle/db`.
