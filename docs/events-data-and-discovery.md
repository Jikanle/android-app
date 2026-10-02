# Events: Collection, Connections And Repeat Attendance

Updated 2026-10-02. Executable offline modules are in `tools/events/`. Shared SQL is
a **proposal for staging**, not a live service. Android still opens the public agenda
and external RSVP. There is no attendee preference form or check-in endpoint in the
app yet, and no real attendance dataset has been imported in this session.

## Collect From People, Not About People

At registration, explain the organizer, purpose, retention and withdrawal channel.
Use an authenticated user ID; do not send emails/WhatsApp numbers to analytics.
Ask for target languages, self-assessed level per language (future reviewed field),
interests and optionally professional areas and conversation goals. Use controlled
tags, with no employer, income, health, political preference or scraped social graph.
Leaving optional questions blank must not block attendance.

Three unchecked, separately revocable purposes: aggregate engagement measurement;
human-mediated introductions; future-event contact. A fourth research/training
consent is **not** bundled with these. `event_preferences` implements the first three
as independent fields, plus a policy version and database-generated timestamp.
`consent_recorded_at` represents the latest state, not an immutable consent audit.
Store the shown consent text/version and access-limited consent receipt in the shared
consent system before production; reconcile with Web's proposed contracts first.

Record one `event_participations` row per user and actual event occurrence:
confirmed RSVP separately from host-verified check-in. For the pilot the host may
import an authorized roster; no email guessing, automatic Luma-to-account matching
or treating calendar/RSVP clicks as attendance. A recurring event gets a distinct
occurrence ID each time. Keep collection-time analytics permission and current opt-in.
Hosts do not get a public participant directory or broad database privileges.

Colombia's purpose-limitation guidance calls for specific informed purposes and new
authorization for a different use. Have the consent/retention design reviewed before
collecting real data; these fields alone do not establish legal compliance.
[SIC guidance](https://www.sic.gov.co/recursos_user/boletin-juridico-feb2017/articulo/datos/el-principio-de-la-finalidad-debe-tenerse-en-cuenta.html).

Proposed retention: identifiable engagement observations 90 days, reviewed aggregates
afterward; operational attendance and consent evidence need an agreed retention basis.
No automated retention job exists yet. Withdrawal stops contact/matching immediately,
excludes historical rows from these analytics queries, and triggers a documented
deletion request process. Do not start collecting until that process has an owner.

## Metrics And Introductions

`docs/event-engagement-metrics.sql` is operator-only:

- Same-language weekly repeat: consenting, verified attendees with at least two
  distinct occurrences in one language / all such attendees that week. Monday weeks
  in `America/Bogota`; exclude the current incomplete week.
- Seven-day consecutive return: first observed verified attendance followed by the
  **next** attendance in the same language within seven elapsed days. An intervening
  other-language event breaks this metric. Exclude immature seven-day windows.
- Report absolute numerator/denominator, collection coverage and missing check-ins.
  SQL suppresses groups below five, which is not a guarantee of anonymization.
- RSVP-to-attendance requires confirmed RSVP evidence; attendance-to-app activation
  requires voluntarily linked identity and matching observation windows. Existing
  `beta_events.event_rsvp_opened` is only click intent and has no event occurrence ID.

`consecutiveLanguageReturn` provides tested reference semantics. It counts distinct
user/event pairs, excludes missing/revoked consent and never treats unverified data
as attendance. SQL still needs execution against staging before operational use.

`suggestConnections` is a deterministic, non-ML prototype using explicitly shared
languages plus interests/professional domains, requiring **both** people to opt in.
It returns reasons, not a compatibility/ability score. The host proposes an intro
privately and both people accept before any contact details are exchanged. No automatic
outreach or ranking for employment decisions. Public social media is not input.

Test repeat attendance by offering two genuinely complementary events in a week,
with optional reminders and a way to decline. Compare descriptive cohorts by language
and acquisition source, not a causal claim: schedules, capacity and missing check-ins
all confound retention. Do not optimize for pressured attendance.

## Source Onboarding

The founder has contacts at IF/Instituto Frances, Colombojaponesa, Kokomi and Sala de
Idiomas BLAA. Their exact public identities/URLs remain to be confirmed. Do not infer
or add random Instagram accounts. Use the private business CRM for names and numbers.

When Alejandro supplies the list, request these fields per institution:

| Field | Example / rule |
|---|---|
| Public institution name | Confirm exact spelling and branch |
| Public website / Instagram URL | Organizer-controlled URL, not a person's profile |
| Private contact reference | CRM ID only; never a phone number in Git |
| Permission | Agenda export, feed/API, or manual review; evidence and expiry |
| Publication rhythm | Weekly/monthly, usual day, timezone; ask the organizer |
| Languages / audience | Confirm per event, not inferred from nationality |
| Feed format | ICS, RSS, JSON, official page, authorized poster |
| Reviewer | Person who approves dates, cancellations and final publication |

Confirmed public entry points (not partner permissions):
[Centro del Japon agenda](https://centrodeljapon.uniandes.edu.co/page/agenda-cultural-y-academica)
and [IBRACO events](https://www.ibraco.org.co/eventos-ibraco/).
The Centro website includes archives; an old month/day without a verified year must
never become an upcoming event. IBRACO retrieval returned a gateway error during
inspection; no events were inferred from that failure.

`sources.json` sets provisional weekly Centro/monthly IBRACO **review** cadence.
These are configurable defaults, not measured publication patterns or scheduled jobs.
Both sources start `adapter: manual`, `automationApproved: false`.

## Discovery Pipeline

1. Prefer organizer-provided calendar/export. Then consider approved public feeds.
2. Verify source terms, access permission, robots policy and rate limits before a
   fetch adapter is enabled. Robots permission is not a license or attendee consent.
3. Keep a minimal source snapshot with observation time and provenance. Parse structured
   JSON-LD with `discovery.ts`; HTML/ICS/RSS transports remain future adapters. No regex
   date extraction, guessed year/timezone or account-login automation.
4. Normalize stable source ID/external occurrence ID, link, title, explicit-offset time,
   venue and source status. Missing/ambiguous fields go to review; language remains null.
5. Deduplicate repeated snapshots; quarantine conflicting identities. Reschedules and
   cancellations require review against the existing occurrence, not a duplicate event.
6. Human confirms language, timezone, recurrence instances, access/price and organizer.
   Only then publish via existing `events` ownership rules. No auto-publication here.

For Instagram-only organizers, accept their supplied agenda/link or an authorized
integration after checking current API capabilities. Never scrape followers, comments,
attendee identities, WhatsApp groups or private posts; never bypass login/CAPTCHA.
OCR of a permitted poster may propose text, but dates/languages require a reviewer.
Meta's automation terms must be reviewed at onboarding; our inspection encountered
a login/block boundary and did not attempt to bypass it.
[Meta terms](https://www.facebook.com/legal/automated_data_collection_terms).

`nextReview` is a tested UTC calendar scheduler helper (weekly +7 days; monthly
same day clamped to month-end). A future worker must persist `last_success`, `next_due`,
ETag/Last-Modified and error/backoff state. Never advance successful-observation time
on a failed request; do not delete events because a page returned empty. Recheck approved
upcoming events for cancellation before attendance. No worker is deployed yet.

```bash
# Input is an organizer-supplied JSON-LD object/array, not a raw HTML page.
deno run --allow-read=tools/events,research-local --allow-write=research-local \
  tools/events/preview.ts centro-japon-uniandes \
  research-local/agenda.json research-local/candidates.json
deno test tools/events tools/research supabase/functions/translate-song/translate_test.ts
```

## Next Two Weeks / Assignable Tasks

| Priority | Owner to confirm | Task and acceptance |
|---|---|---|
| P0, week 1 | Alejandro | Supply partner list and permission evidence; onboard two agendas with confirmed cadence |
| P0, week 1 | Platform owner + Web owner | Reconcile ADR 0004 with Web consent schema; test SQL twice in staging and anon/A/B/service permissions |
| P0, week 1 | Rafa + platform owner | Build optional preference form and withdrawal/delete paths, preserving brand; no public directory |
| P1, week 1 | Event host | Validate authorized roster import and check-in evidence with synthetic identities first |
| P1, week 2 | Platform owner | Persist candidate review queue and approved source transport; cancellation/update fixtures; no auto-publish |
| P1, week 2 | Alejandro + David Daza | Run two related-language events, collect only agreed data and record attendance coverage |
| P1, week 2 | Robert | Reconcile SQL cohort counts with fixtures; distinguish repeat visits from learning outcomes |

These are backlog items, not completed integrations or externally assigned issues.
