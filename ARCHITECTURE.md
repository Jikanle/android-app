# Jikanle Android Architecture

## Overview

```text
Android app (Compose + ViewModels)
  |
  | Ktor engine through supabase-kt
  v
Supabase
  |-- Auth: email and OAuth sessions
  |-- Postgres: users, events, songs, lessons, rooms, companion matches
  |-- Realtime: room chat, presence, playback state
  |-- Storage: future audio, covers, LRC files

Room cache sits beside the network layer for offline reads.
```

Android is a client of the shared Supabase backend. It does not own product contracts or migrations. Until `jikanle/db` and `jikanle/lesson-content` are ready, this repo carries only a bundled Fuyu no Hanashi seed for first-launch continuity.

## Module Layout

The current project is a single Android app module using package boundaries:

- `core/design` — Material 3 theme, color tokens, typography, shared UI primitives.
- `core/domain` — serializable domain models and repository interfaces.
- `core/data` — Supabase adapters, Room entities/DAOs, mappers, bundled seed fallback.
- `core/di` — Hilt modules for repositories, Supabase, Room, dispatchers.
- `feature/auth` — email and Google sign-in surface.
- `feature/lesson` — slide deck reader for the seed lesson and future lessons.
- `feature/songbridge` — translated-song demo surface retained from the current MVP.
- `feature/profile` — lightweight profile scaffold with hobby tags.
- `navigation` — Compose navigation and deep link route constants.

The prompt's target multi-module structure is intentionally deferred until module boundaries are worth the Gradle overhead. `build-logic/` is reserved for convention plugins when that split happens.

## Domain Model

### User

- `id: String`
- `email: String?`
- `displayName: String`
- `avatarUrl: String?`
- `roles: List<Role>`
- `nativeLanguage: String?`
- `targetLanguages: Map<String, Level>`
- `hobbies: List<String>`
- `city: String?`
- `createdAt: String?`

### Event

- `id: String`
- `title: String`
- `startsAt: String`
- `endsAt: String`
- `venueName: String`
- `address: String?`
- `city: String`
- `hostId: String?`
- `lumaUrl: String?`
- `songOfTheNightId: String?`
- `roomId: String?`
- `capacity: Int?`
- `published: Boolean`

### Song

- `id: String`
- `titleOriginal: String`
- `titleRomanized: String?`
- `artist: String`
- `language: String`
- `audioUrl: String?`
- `coverUrl: String?`
- `durationMs: Long?`
- `releaseYear: Int?`
- `lrcUrl: String?`
- `vocabularySeedIds: List<String>`

### Lesson

- `id: String`
- `songId: String`
- `creatorId: String?`
- `title: String`
- `description: String?`
- `languageTarget: String`
- `languageExplanation: String`
- `level: Level`
- `slideDeck: SlideDeck`
- `vocabularyPicks: List<Vocabulary>`
- `discussionPrompts: List<String>`
- `culturalNotes: String?`
- `isPaid: Boolean`
- `priceCop: Int?`
- `visibility: LessonVisibility`
- `publishedAt: String?`

### Room

- `id: String`
- `eventId: String?`
- `lessonId: String?`
- `title: String`
- `description: String?`
- `hostId: String?`
- `startsAt: String?`
- `endsAt: String?`
- `isLive: Boolean`
- `memberCount: Int`

### CompanionMatch

- `id: String`
- `userAId: String`
- `userBId: String`
- `status: MatchStatus`
- `sharedRoomId: String?`
- `languageTarget: String`
- `reason: String?`
- `createdAt: String?`

## Realtime Channels

- `room:{id}:chat` — short room messages and post-event prompts.
- `room:{id}:presence` — who is currently practicing or listening.
- `room:{id}:playback` — future shared playback state for listening rooms.

Realtime is not implemented in this bootstrap. It is a contract reservation for the shared backend.

## Roles

- `Learner` — default participant.
- `Creator` — can author lessons.
- `Host` — can host rooms/events.

Roles are additive, not exclusive. Verification is manual by admin.

## Slide Deck JSON Schema

`Lesson.slide_deck` is a JSON object with `slides: []`. Android supports these seed slide types:

- `intro`: `title`, `subtitle`, `notes_es`, `notes_en`
- `listen_first`: `instruction_es`, `instruction_en`
- `vocabulary`: `items[]` with `surface`, `reading`, `meaning_es`, `meaning_en`
- `grammar`: `pattern`, `title_es`, `title_en`, `body_es`, `body_en`
- `cultural`: `title`, `body_es`, `body_en`
- `discussion`: `prompts_es[]`, `prompts_en[]`
- `listen_second`: `instruction_es`, `instruction_en`
- `outro`: `next_es`, `next_en`

The domain model maps these into sealed `Slide` classes for rendering. The seed reader is a fallback adapter, not a new Android-owned backend schema.

## Deep Links

- `jikanle://lesson/{id}`
- `jikanle://room/{id}`
- `jikanle://event/{id}`
- `https://jikanle.com.co/app/*`

The manifest declares these routes. `navigation/NavGraph.kt` owns the Compose destinations.

## Contracts With Sibling Repos

- `jikanle/db`: Android reads Supabase tables and RLS-scoped views from this repo once migrations are canonical.
- `jikanle/lesson-content`: Android fetches structured Lesson JSON, LRC files, and curated vocabulary from this repo or its generated storage output.
- `jikanle/brand`: Android consumes palette, typography, logo, and exportable app icon assets.
- `jikanle/web-app`: Android mirrors DTOs and UX contracts already proven on web.
