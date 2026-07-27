# Jikanle Android Agent Brief

## 1. Context

Jikanle is a language-learning brand built on a single belief: **most hours people spend trying to learn a language are also hours they spend alone, and that is the bug, not the user.** Music is the glue between *learning* and *belonging*. Jikanle starts as small in-person evenings in Bogota, then extends online so the bond formed in the room continues at home.

- **Tagline:** *Time that counts twice.*
- **Name origin:** *jikan* (時間) = Japanese for "time"; *乐* = music/joy.
- **Logo character:** 乐.
- **Founder:** Alejandro Sanchez Poveda (`alesanchezpov@gmail.com`), 19, CS at UNAL Bogota, violinist, learning Japanese and Chinese.

### The three temporal layers

1. **Now: The Room** — small physical events in Bogota. RSVPs via Luma at `https://luma.com/Jikanle?k=c`. First event: Casa Alternativa, Saturday 5-8 PM, *cancion de la noche* = **Fuyu no Hanashi**.
2. **Next: The Continuity** — the web app (`jikanle.com.co`, Next.js 14 + Supabase) and this Android app keep people practicing together after the night ends.
3. **Horizon: The Instrument** — a future violin/hardware product called "Jikanle Phrase". Out of scope now; the data model must not preclude it.

### What this app is not

Do not add these without a linked GitHub issue explaining why the exception strengthens the product:

- Flashcards or SRS.
- Daily streaks, XP, leaderboards.
- Auto-mined sentences from arbitrary videos.
- Auto-generated quizzes from lyrics.
- Solo "learn at your own pace" courses.
- Generic chatbot tutoring.
- Social-network chrome such as likes, public feeds, or follower counts.
- Emoji in UI chrome.

When in doubt, ask: **"Does this make sense for the eight people who met at Casa Alternativa last Saturday and want to keep talking?"**

### Sibling repos

- `jikanle/web-app` — canonical web client and first source for backend contracts.
- `jikanle/brand` — logo, color, and typography tokens.
- `jikanle/db` — Supabase migrations, RLS policies, seed data.
- `jikanle/lesson-content` — structured lesson JSON and LRC files.
- `jikanle/research` — LAMIR/MIR experiments for future alignment.
- `jikanle/docs` — private founder brief and playbooks.

## 2. Brand System

The palette and typography are locked. Read `BRANDING.md` before changing any UI.

Android uses Material 3 tokens backed by:

- `ink` `#1F1438`
- `ink-soft` `#5B4B7A`
- `ink-faint` `#9C8FB8`
- `primary` `#6D2DD3`
- `pink` `#E63B96`
- `blue` `#3B5BDB`
- `paper` `#FBF7F8`
- `paper-deep` `#F2EAF5`
- `paper-rule` `#E5D6E5`

Spanish is the default resource language. English lives in `res/values-en/strings.xml`. Japanese and Chinese appear as content, not UI language. Never use "AI" as in-app marketing copy.

## 3. Tech Stack

| Concern | Choice |
|---|---|
| Language | Kotlin 2.0+ |
| UI | Jetpack Compose, Material 3 |
| Min / Target SDK | 26 / 36 |
| Audio | Media3 / ExoPlayer |
| Network | Ktor client |
| Backend SDK | Supabase Kotlin (`auth`, `postgrest`, `realtime`, `storage`) |
| Local cache | Room |
| DI | Hilt |
| Async | Coroutines + Flow |
| Image loading | Coil |
| Build config | Gradle Kotlin DSL |
| CI | GitHub Actions |
| Signing | GitHub encrypted secrets |
| Distribution | Play Console internal testing first |

Do not introduce Firebase, RxJava, Retrofit in parallel with Ktor, or another backend-as-a-service.

## How To Work In This Repo

- Read `ARCHITECTURE.md` before touching a feature module.
- Keep commits small and conventional: `feat(lesson): render vocabulary slide`.
- Update `ROADMAP.md` when finishing a task or finding a blocker.
- Preserve `co.com.jikanle` as the Android namespace/application ID unless a migration plan is approved.
- Do not inspect, print, edit, or commit `local.properties`.
- Prefer `./gradlew :app:assembleDebug` over `gradle build`; this repo uses the wrapper.

## Where To Find Things

- Brand: `BRANDING.md`
- Domain model: `ARCHITECTURE.md` section "Domain Model"
- Roadmap: `ROADMAP.md`
- Sibling repo contracts: `ARCHITECTURE.md` section "Contracts With Sibling Repos"
- Play release: `docs/play-store-launch.md`
- Event flow: `docs/event-playbook.md`

## Session End Checklist

Every session should end with:

- A commit, unless the user explicitly asks for no commit.
- An updated `ROADMAP.md` or a session log explaining why it did not change.
- Passing `./gradlew :app:assembleDebug` and relevant tests.
- No committed secrets (`local.properties`, keystores, `google-services.json`).
