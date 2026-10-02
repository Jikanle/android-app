# ADR 0004: Research Contracts And Consent-Aware Events

Date: 2026-10-02. Status: proposed for shared backend review; local prototypes implemented.

## Context

The team needs independent music, language and pedagogy work, while Android and Web
consume one reviewed product. Six people do not need six deployments. Research is
not production truth; public event listings do not authorize attendee profiling.

## Decision

Keep one shared Supabase backend and one Android app module. Establish boundaries
through versioned, reviewed artifacts before creating repositories or microservices:

| Boundary | Owns | Does not own |
|---|---|---|
| Linguistics | Meaning, register, deixis, morphology, human-reviewed adaptation candidates | Audio quality or learner efficacy |
| Music input | Licensed source identity, segment times, alignment, beat/note/phrase observations | Translation correctness |
| Music output | Authorized performances, articulation, breaths, timing and mixing comparisons | Universal song-quality score |
| Pedagogy | Learner/objective suitability, activities, pre/post/delayed assessments | Retention interpreted as learning |
| Events | Partner agenda, occurrence identity, RSVP/check-in evidence, optional preferences | Scraped personal profiles |
| Product/platform | Auth, contracts, delivery, cache, observability, permissions | Invented research measurements |

Research lives behind adapters, initially `tools/research/` and `tools/events/` as
offline development utilities. They have no network, storage credentials or public
API. The canonical SQL currently lives in `supabase/`, linked by the business
backend. Later extract these utilities to the backend/research repo at one ownership
handoff, not by maintaining duplicate copies. Android consumes previews now and
reviewed shared content later. No new financial/healthcare service is introduced.

## Changed Contracts

- `pt` joins `ja,en,es,zh` in `song_lyric_lines.language`,
  `song_translations.source_language/target_language`, `song_vocabulary.language`
  and the translation function's accepted codes. Existing projects need
  `language_portuguese.sql`; reapplying CREATE TABLE does not alter CHECK constraints.
- Bundled/imported `DemoTranslation.vocabulary` is an optional localized glossary.
  It is NOT a new SQL column. SQL currently lacks glossary explanation-language
  identity; Web/backend must agree that extension before remote glossary import.
- `event_preferences`: private user ID, declared languages/interests/professional
  domains/goals, three independent opt-ins, consent version and server timestamp.
- `event_participations`: event occurrence/user primary key, optional RSVP and
  verified check-in timestamps, verification method, collection-time analytics
  authorization/version and receipt time. Only a trusted operator writes it.
- `tools/events/discovery.ts`: pending candidates with source identity/link,
  title, explicit-offset start/end, venue, source status and **unknown** language.
  No new public `events` are created automatically.
- `tools/research/study.ts`: version-1 private JA-PT text study, stable line IDs,
  provenance, drafts/notes and nullable audio reference/interval. No trained model,
  acoustic assessment or synchronization is claimed by exporting a preview.

## Consequences And Gates

Web must review these additions against its proposed communities/preferences/consent
tables before applying SQL. Do not create competing user preference sources of truth.
The Python Songbridge language enum/prompt needs a coordinated Portuguese update;
that adapter was not edited in this Android session.

`event_engagement.sql` is a staging proposal, not deployed. Before rollout test anon,
user A, user B and service role: no public reads, no cross-user preference changes,
no client-written attendance. Test deletion cascades and consent withdrawal.
Keep service credentials outside clients. Deploy consent UI, revocation/deletion
flow and controlled attendance importer together, not SQL alone.

The current translate-song endpoint still needs authorization/ownership review,
atomic content replacement and quota controls before a public generation beta.
Portuguese support is not permission to expose that endpoint or process recordings.
