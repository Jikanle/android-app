# Android Beta Contract

Updated 2026-09-09. Code is implemented; live deployment and RLS integration tests remain unverified.

## Ownership And Events

One Supabase system of record serves Android and Web. Shared SQL currently resides in Android `supabase/`, exposed through business-root `backend/supabase/`. Coordinate backend deployment; do not create more repositories.

Android reads public `events` and `organizations`. Event DTO additions: `organization_id: uuid?`, `language: text?`, `level: text?`, `summary: text?`, `tags: text[]`, `timezone: text?`, `recurrence: text?`, `lat/lng: float?`, `visibility: text`. Organization addition in `organization_communities.sql`: `community_url: text?`, a public HTTPS invitation or landing page. No member lists or private invitation secrets.

Cache replacement is transactional. Refresh failure preserves previous events. A Luma agenda link remains available with empty/unavailable Supabase. No event dates are fabricated. Opening RSVP/community URLs is not proof of attendance or membership. Calendar insertion opens the device confirmation UI; event times are displayed in Bogota time with zone annotation.

## Learning

The creator-authored `Lesson.slide_deck` contract is unchanged. The reader consumes the requested navigation `lessonId`; unknown IDs show an empty state. The reserved Fuyu seed ID explicitly loads localized bundled content. Progress (`lessonId`, zero-based `step`, `completed`) is device-local in separate Room storage. Final acknowledgement marks completion, not educational achievement. Uninstall clears progress. Accounts on the same device share that local progress; cloud progress synchronization is deferred.

The seed opens a Spotify search, not an embedded player or a verified recording. Research-derived timing, pronunciation scores and audio capture are not implemented.

## Observations v1

`supabase/beta_events.sql` defines the table. PostgREST upsert uses `on_conflict=id`, `ignoreDuplicates=true`, no returned rows. Required: `id: uuid`, `user_id: uuid` (current auth identity), `session_id: uuid` (process lifetime), `event_name: text`, `app_version: text`, `language: text`, `occurred_at: timestamptz`. Optional: `lesson_id: text` (allows demo IDs), `rating: int?` (1-5 only for feedback). Server supplies `received_at`. No arbitrary properties, emails, lyrics, recordings or written responses.

Events: `app_opened`, `lesson_opened`, `lesson_started` (first advance), `lesson_completed` (final acknowledgement), `vocabulary_opened` (advance into vocabulary), `event_rsvp_opened`, `community_opened`, `feedback_submitted`. Aggregate distinct users/lesson IDs to avoid counting repeat reviews as new learners. Other requested research events are deferred.

`app_opened` records at most once per process/user/Bogota calendar day, on authenticated opt-in or foreground entry. It is a daily presence marker, not a count of every foreground session. A process kept alive overnight can still produce the next day marker.

Usage sharing defaults off and requires sign-in. Guest use creates no anonymous auth identity and sends no metrics. Rating feedback is explicitly submitted independently of the usage switch. Disabling sharing or signing out clears pending submissions, not server history. Operators must establish retention and deletion handling before widening the cohort.

The outbox retains the newest 500 records and flushes up to 50 per attempt. Retries run on session restoration, tracked interactions and feedback Retry. No background delivery guarantee. The UI reports pending when offline or backend deployment fails. Only records owned by the current user are submitted. Stable UUIDs deduplicate retries after a lost response. Storage errors in usage tracking do not interrupt learning.

## Deployment Acceptance

1. Apply `beta_events.sql` in staging with backend owners; use two real test identities.
2. Confirm anon insert/select is denied and user A cannot insert as B or read B's observations.
3. Confirm client update/delete is denied and duplicate UUID retries store one row.
4. Invalid event names, missing feedback ratings and values outside 1-5 must fail.
5. Airplane mode: feedback queues; reconnect and Retry delivers once.
6. Switch A to B: no A record is sent as B. Disabling sharing clears the pending queue.
7. Run `docs/beta-metrics.sql` as an operator, not from the app.

Web impact: existing calendar unaffected; Web may adopt the same opt-in contract later. Backend impact: additive SQL, RLS verification, data retention/deletion and query review. These are consenting signed-in tester metrics, not all installs, crash-free sessions, total MAU or learning outcomes. Client clocks are untrusted; use `received_at` for operational debugging. Add server abuse controls before public traffic.
