# Event preferences and verified attendance

Implementation: 2026-10-03. Not deployed. The shared migration is
`supabase/event_engagement.sql`; Android is a client, not a second backend.

## Reconciliation decision

Web's `lib/account.ts` reads `account_preferences`. Its `complete_website_onboarding`
contract includes target/native languages and separate email/WhatsApp marketing
permissions. Web tests reference `website_v02.sql`, but that migration is absent in
this checkout and in the linked canonical backend directory. Do not recreate or
replace it from a guess. Recovery and staging integration remain a release gate.

`event_preferences` is a purpose-specific extension, NOT a replacement profile:

| Concern | Owner / contract |
|---|---|
| Display name, native/target languages, phone | Web/shared `account_preferences` |
| Email/WhatsApp marketing | Web account consent history; unchanged by Android |
| Languages desired at events, interests, domains, goals | `event_preferences`, optional |
| Event introductions and attendance metrics | Independent `matching_opt_in`, `metrics_opt_in` |
| Model training or publication rights | Separate content-rights contract; never implied |
| In-app click telemetry | Existing `beta_events`; NOT attendance |

The legacy `contact_opt_in` field is reserved and always false for the new RPC.
No contact UI or mailing integration is enabled. Web must use the same event RPCs
if it adds these controls. General account withdrawal must eventually orchestrate
both purpose sets explicitly, not silently change unrelated permissions.

## Android

Beta/profile -> Event preferences. Controlled language, interest, professional-area
and goal chips, two default-off switches, Save and confirmed Withdraw. Uses the
existing Material theme. Signed-out users are directed to sign in. A missing
migration/network failure is an error, never an apparently successful local save.
Withdrawal remains available after a failed load. It requires a server response;
there is no offline consent outbox. An email fallback is displayed on failure.

`EventPreferencesRepository` -> PostgREST RPC. No direct Supabase access from UI.
No cache shared across accounts. Sign-out clears visible state. ES/EN resources.

## Shared API

- `save_event_preferences(p_languages text[], p_interests text[], p_domains text[],
  p_goals text[], p_matching boolean, p_metrics boolean)`: own identity from JWT,
  allowlisted tags; database assigns policy `events-v1` and timestamps.
- `withdraw_event_preferences()`: clears tags, disables all event purposes and
  permanently clears existing attendance analytics eligibility. Reactivation does
  not reactivate old observations. Does not delete operational attendance/account.
- `event_consent_history`: server-written purpose/version receipts, owner-readable,
  no client mutation. This is a technical record, not a legal compliance certificate.
- `import_verified_attendance(p_event_id uuid, p_rows jsonb, p_evidence_ref text)`:
  event host or organization editor only. Roster rows contain **only** `user_id`
  and `checked_in_at` with explicit timezone; 1-100 rows, distinct UUIDs, bounded
  event window, no future check-in. Evidence is an opaque `roster:...` reference
  to a private organizer record, not a public URL or attendee email.

Import is atomic. A missing account or invalid row aborts the batch. Existing verified
rows are unchanged; existing RSVP-only rows gain check-in evidence without losing RSVP.
Response `{verified, unchanged}` supports retries after lost responses. Import does
not infer RSVP and never maps email/name to an account. The host must review evidence;
the database cannot prove physical presence. Corrections require a reviewed admin
procedure; this RPC intentionally does not rewrite verified history.

Analytics permission requires a grant recorded **before** check-in, current permission,
and no later withdrawal. Row locks serialize import and withdrawal for existing
preferences; absence of permission fails closed. Legacy unreceipted grants are not
backdated. Both SQL metrics queries additionally require current permission.

## Operator workflow

Keep authorized roster files under ignored `research-local/`, not Git/Drive docs.
Obtain UUIDs through voluntary account linking; never assume RSVP equals attendance.
Example shape uses synthetic IDs, not real people:

```json
{
  "event_id": "00000000-0000-4000-8000-000000000001",
  "evidence_ref": "roster:host-session-001",
  "rows": [{"user_id": "00000000-0000-4000-8000-000000000002", "checked_in_at": "2026-10-03T18:00:00-05:00"}]
}
```

```bash
# Offline only; does not need credentials.
deno run --allow-read=research-local tools/events/import-attendance.ts research-local/roster.json
# In STAGING, using an authenticated event-host token and public anon key.
# Supply environment privately; do not paste tokens into chat or shell history.
deno run --allow-read=research-local \
  --allow-env=SUPABASE_URL,SUPABASE_ANON_KEY,HOST_ACCESS_TOKEN \
  --allow-net=YOUR-STAGING-PROJECT.supabase.co \
  tools/events/import-attendance.ts research-local/roster.json --apply
```

No service-role key belongs in this command. No roster is uploaded by default.

## Verification and remaining gates

`tools/events/check-database.mjs` runs actual SQL in disposable PGlite 0.5.8,
the same engine/version already used by Web. Applies migration twice; tests
anon/own/other/host roles, bad tags, forged writes, duplicate retry, future check-in,
withdrawal, regrant without retrospective consent and execution of aggregate queries.
It stubs Supabase Auth functions; therefore hosted Auth/PostgREST needs staging tests.

```bash
# Point to an installed PGlite dist/index.js; CI installs an isolated pinned copy.
PGLITE_MODULE=/absolute/path/to/pglite/dist/index.js node --test tools/events/check-database.mjs
deno test tools/events
```

Before real users: recover Web migration; review purpose text/retention with the
owner; test two real staging identities through PostgREST; exercise airplane mode,
retry, logout/account change, large text and portrait/landscape on a phone. Verify
withdrawal from Android removes the participant from operator metrics. Assign
Alejandro the deletion-request inbox and document deletion completion, not merely
an email address. Automated retention/deletion is not implemented.
