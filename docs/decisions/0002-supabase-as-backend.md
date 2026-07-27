# ADR 0002: Supabase As Backend

## Status

Accepted — 2026-07-27

## Context

Jikanle needs shared user accounts, room/event data, lessons, vocabulary, storage for covers/audio/LRC files, and eventual realtime room presence. The Android app is one client beside the web app, not a separate product backend.

## Decision

Use Supabase as the shared backend for Android and web:

- Postgres with RLS for relational data and client-safe access.
- Auth for email and OAuth sessions.
- Storage for future audio, cover art, and lyric timing files.
- Realtime for chat, presence, and playback channels.
- Kotlin SDK support through `supabase-kt`.

## Alternatives Considered

- Firebase: strong mobile tooling, but weaker relational modeling and higher risk of schema divergence from the web app.
- Custom Ktor backend: maximal control, but premature and too much operational load for the MVP.

## Consequences

Android must respect shared DTOs and RLS. Schema changes belong in `jikanle/db`, with Android mapping updates reviewed against the web contract.
