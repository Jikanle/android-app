# ADR 0005: Opt-In Rights Are Purpose-Specific

Date: 2026-10-02. Status: proposed.

## Context

Jikanle wants to build cover-analysis and, later, generative audio models in a way that
respects creators. Existing song and generation tables have broad `license` and
`audio_processing_allowed` fields. Those fields cannot express the difference between
private analysis, translation, a cover recording, audiovisual synchronization, public
distribution, model training and voice likeness.

## Decision

Use a private rights ledger with immutable evidence references and purpose-specific
grants. Processing and training workers fail closed unless the exact asset, purpose,
territory, time window and provider transfer policy are approved. Store contracts in
restricted storage; Postgres stores hashes, scope and review state. Keep rights decisions
independent from content quality and pedagogical review.

The first catalog uses public-domain and directly licensed partner material. Commercial
works can remain private research fixtures. No Jikanle UI or API will call a cover
permission “fair use” or infer training permission from a public upload.

## Alternatives considered

- One boolean `opt_in`: rejected because it hides different uses and rights holders.
- Public upload implies consent: rejected because availability is not a grant.
- Fair-use-first product: rejected because exceptions are jurisdiction-specific and
  uncertain for adaptation, sync, public video and training.
- Separate microservice for rights: deferred. The ledger is a shared backend contract;
  services can evolve later without duplicating policy.

## Consequences

More partner onboarding and review work is required. In exchange, Jikanle can produce a
dataset manifest, exclude withdrawn assets, report usage and pursue an external fair-
training audit with evidence. Model output and revenue terms remain contract questions;
the schema does not promise ownership or royalties.

Apply `supabase/content_rights.sql` only after Web/backend/legal review and two-user RLS
testing. It is not applied by this session.
