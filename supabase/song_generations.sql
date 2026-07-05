-- ============================================================================
-- song_generations — per-user AI re-sung tracks (Node 4 output).
-- Additive + idempotent. Run in the Supabase SQL editor after schema.sql.
--
-- Each row is one generated audio take owned by the user who made it. The audio
-- file lives in the PRIVATE `song-renders` storage bucket under <owner_id>/…, so
-- only the owner can read it (copyright: generated covers are not public).
-- ============================================================================

create table if not exists public.song_generations (
    id             uuid        primary key default gen_random_uuid(),
    owner_id       uuid        not null references auth.users (id) on delete cascade,
    song_id        uuid        not null references public.songs (id) on delete cascade,
    translation_id uuid        not null references public.song_translations (id) on delete cascade,
    target_language text       not null check (target_language in ('ja','en','es','zh')),
    provider       text        not null default 'elevenlabs',
    prompt         text,
    audio_path     text        not null,          -- object path inside the song-renders bucket
    audio_url      text,                           -- signed URL (expires; refresh via the function)
    duration_ms    int,
    sense_distance numeric,                        -- CLAP 1-cos(original,candidate); null until scored
    created_at     timestamptz not null default now(),
    -- one take per user per translation → avoids regenerating duplicates (and re-spending)
    unique (owner_id, translation_id)
);
create index if not exists song_generations_owner_idx on public.song_generations (owner_id, created_at desc);

grant select on public.song_generations to authenticated;

alter table public.song_generations enable row level security;

-- Owners read their own takes. Inserts/updates happen only via the Edge Function
-- (service role, which bypasses RLS), so there is deliberately no user insert policy.
drop policy if exists song_generations_select_own on public.song_generations;
create policy song_generations_select_own on public.song_generations
    for select to authenticated using (owner_id = auth.uid());

-- ---- Private storage bucket for the rendered audio -------------------------
insert into storage.buckets (id, name, public)
values ('song-renders', 'song-renders', false)
on conflict (id) do nothing;

-- Files are stored as `<owner_id>/<translation_id>.mp3`; owners read only their folder.
drop policy if exists song_renders_owner_read on storage.objects;
create policy song_renders_owner_read on storage.objects
    for select to authenticated
    using (bucket_id = 'song-renders' and (storage.foldername(name))[1] = auth.uid()::text);
