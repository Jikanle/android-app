# ADR 0001: Record Architecture Decisions

## Status

Accepted — 2026-07-27

## Context

Jikanle spans Android, web, Supabase, event operations, brand, and future lesson content repositories. Architectural choices can easily become implicit and split across agent sessions.

## Decision

We will record durable architectural decisions as ADRs in `docs/decisions/`. Each ADR includes status, context, decision, consequences, and links when applicable.

## Consequences

- Future agents can see why a choice was made before replacing it.
- Changes that affect sibling repos can be reviewed explicitly.
- ADRs are lightweight enough to write during normal feature work.
