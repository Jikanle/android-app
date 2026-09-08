# ADR 0003: Supabase Now, GCP-Ready AI Later

## Status

Accepted — 2026-09-08

## Context

Jikanle needs an MVP that works quickly and cheaply. Supabase already fits the current Android app: auth, Postgres, RLS, storage, realtime, and Kotlin SDK support. At the same time, the long-term product will likely need heavier audio and machine-learning workloads for transcription, alignment, translation assistance, and music-aware lesson preparation.

The founder wants the system to stay cloud-oriented but not permanently locked to one provider. Google Cloud is a strong future target because Android distribution, Google account auth, Cloud Run, Cloud SQL, Cloud Storage, and the Magenta/music-ML ecosystem align with the product direction.

## Decision

Keep Supabase as the MVP backend for user accounts, relational product data, access control, and simple storage. Do not build a Python/FastAPI backend into the visible MVP.

Design future audio/ML work as an async job service that can start locally in WSL and later deploy to Google Cloud Run. Android must depend on repository interfaces and stable job contracts, not directly on vendor-specific implementation details.

## Consequences

- MVP velocity stays high because Supabase remains the main backend.
- Android stays portable by isolating backend access behind `core/domain/repository` interfaces.
- Future FastAPI work should expose job-oriented endpoints such as submit job, get status, and get result.
- Future GCP migration should prefer portable Postgres concepts before adding provider-specific services.
- No AI/audio generation feature becomes visible until content rights, cost, and latency are clear.

## Migration Direction

1. MVP: Android -> Supabase.
2. Near term: Supabase tables store AI/audio job status; FastAPI worker processes jobs.
3. GCP-ready phase: FastAPI on Cloud Run, Postgres on Cloud SQL or Supabase-compatible Postgres, objects in Cloud Storage or S3-compatible storage.
4. Later: specialized audio/ML workers can use GPU or managed inference if product usage justifies the cost.
