# Jikanle — Android app

Shared cross-project context (web app, shared Supabase backend, Songbridge) lives at the
Jikanle business root and is imported below. Also read `STATUS.md` next to that file for
in-flight cross-agent notes. (The import is skipped silently on machines where the path
doesn't exist.)

@~/Documents/1_Projects/Jikanle_Business/CLAUDE.md

## Build

- `JAVA_HOME` is not set system-wide — use Android Studio's bundled JDK:
  `export JAVA_HOME="$HOME/.local/share/JetBrains/Toolbox/apps/android-studio/jbr"`.
- Build: `./gradlew :app:assembleDebug`.
- Supabase credentials come from `local.properties` (gitignored) → `BuildConfig`:
  `SUPABASE_URL`, `SUPABASE_ANON_KEY`.
- Room must stay ≥ 2.7.2 — AGP 9 forces KSP2 and Room 2.6.1's processor breaks under it.
  Don't downgrade.

## Architecture

- Offline-first repositories: Room cache + supabase-kt, Hilt DI, one shared `AppJson`
  kotlinx-serialization instance.
- `supabase/schema.sql` in this repo is the **canonical** schema for the whole product
  (web + Android). If you change it, both clients are affected — note the change in the
  business root's `STATUS.md`.
- This app is a Supabase **client only**: no app server, no calls to/from the web app;
  coordination happens through shared Supabase state (Auth, Postgres + RLS, Realtime,
  Storage).
