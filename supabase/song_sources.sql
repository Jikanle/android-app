-- Standalone, idempotent migration for the Songbridge source resolver.
-- Run in Supabase SQL Editor when schema.sql was applied before song_sources existed.

create table if not exists public.song_sources (
    id                       uuid        primary key default gen_random_uuid(),
    provider                 text        not null
                                         check (provider in ('youtube','spotify','jamendo','self_hosted')),
    source_id                text        not null,
    title                    text        not null,
    artist                   text        not null,
    thumbnail_url            text,
    external_url             text        not null,
    embed_url                text        not null,
    playback_type            text        not null check (playback_type in ('embed','direct_audio')),
    audio_url                text,
    audio_processing_allowed boolean     not null default false,
    license                  text,
    warnings                 jsonb       not null default '[]'::jsonb,
    created_at               timestamptz not null default now(),
    updated_at               timestamptz not null default now(),
    unique (provider, source_id),
    check (
        (playback_type = 'embed' and audio_url is null and audio_processing_allowed = false)
        or
        (playback_type = 'direct_audio' and audio_url is not null)
    )
);

create index if not exists song_sources_provider_source_idx
    on public.song_sources (provider, source_id);

alter table public.song_sources enable row level security;
grant select on public.song_sources to authenticated;

drop policy if exists song_sources_authenticated_read on public.song_sources;
create policy song_sources_authenticated_read on public.song_sources
    for select to authenticated using (true);

-- Writes have no browser policy. The resolver writes with a server-side key.
