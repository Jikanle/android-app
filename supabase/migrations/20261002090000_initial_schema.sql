-- ============================================================================
-- Jikanle — Supabase schema (0001)
-- Derived from the Kotlin domain models in
--   app/src/main/java/co/com/jikanle/core/domain/model/
-- and the Android Build Brief (§6 entities, §7 roles, §8 matching).
--
-- Apply via the Supabase SQL editor or `supabase db push`.
--
-- NOTE ON OWNERSHIP: the Build Brief (§2/§9) says the *web* app owns the canonical
-- backend under db/migrations/. That folder does not exist yet, and this Supabase
-- project is empty, so this file is the de-facto source of truth for now. When the
-- web migrations materialize, reconcile the two (ideally move this into the shared
-- db/migrations/ folder both apps read).
--
-- Design rule that drove every choice here:
--   Postgrest deserializes each row DIRECTLY into the @Serializable data classes,
--   and the same rows serve web + Android. So every List/Map field a data class
--   reads INLINE is an array or jsonb COLUMN — never a join table. Column names
--   match the @SerialName values exactly.
--
-- NORMALIZATION NOTE: arrays-of-uuid (member_ids, joined_event_ids,
-- vocabulary_seed_ids, playlist_song_ids) can't carry FK constraints in Postgres
-- and don't scale for huge membership. They are kept as arrays to match the data
-- classes. The global "Open Room" expresses membership via RLS (all authed users
-- can read), NOT by stuffing every user id into member_ids.
-- ============================================================================

create extension if not exists pgcrypto;  -- gen_random_uuid()

-- Enums are modeled as TEXT + CHECK (a new value is a constraint edit, not
-- ALTER TYPE). Values are the lowercase @SerialName strings the Kotlin enums emit:
--   level             : beginner | intermediate | advanced
--   role              : learner | creator | host | admin
--   lesson_visibility : public | event_only | unlisted
--   match_status      : pending | accepted | declined

-- ============================================================================
-- profiles  (domain: User) — 1:1 with auth.users
-- ============================================================================
create table if not exists public.profiles (
    id                 uuid        primary key references auth.users (id) on delete cascade,
    display_name       text        not null,
    avatar_url         text,
    native_languages   text[]      not null default '{}',
    target_languages   text[]      not null default '{}',
    -- Map<String, Level> e.g. {"ja":"beginner"} — validated app-side
    level_by_language  jsonb       not null default '{}'::jsonb,
    hobbies            text[]      not null default '{}',
    roles              text[]      not null default array['learner']::text[]
                         check (roles <@ array['learner','creator','host','admin']::text[]),
    joined_event_ids   uuid[]      not null default '{}',  -- no FK (array)
    created_at         timestamptz not null default now()
);

-- ============================================================================
-- songs  (domain: Song) — no owner in the model; writes are admin-only
-- ============================================================================
create table if not exists public.songs (
    id                   uuid        primary key default gen_random_uuid(),
    title_original       text        not null,
    title_romanized      text,
    artist               text        not null,
    language             text        not null,
    audio_url            text,       -- songs/ bucket
    cover_url            text,
    duration_ms          bigint,
    release_year         int,
    lrc_url              text,       -- lyrics/ bucket (LRC file)
    vocabulary_seed_ids  uuid[]      not null default '{}',  -- -> vocabulary.id (no FK)
    created_at           timestamptz not null default now()
);

-- Added separately so existing Supabase projects can re-apply this file safely.
alter table public.songs
    add column if not exists is_public_domain boolean not null default false;

-- Normalized external playback sources. Provider media is embedded or linked;
-- Jikanle never downloads YouTube/Spotify audio.
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

-- ============================================================================
-- translated-song MVP
-- line_index is zero-based everywhere and follows Songbridge JSON array order.
-- ============================================================================
create table if not exists public.song_lyric_lines (
    id              uuid        primary key default gen_random_uuid(),
    song_id         uuid        not null references public.songs (id) on delete cascade,
    line_index      int         not null check (line_index >= 0),
    language        text        not null check (language in ('ja','en','es','zh')),
    text            text        not null,
    transliteration text,
    created_at      timestamptz not null default now(),
    unique (song_id, language, line_index)
);
create index if not exists song_lyric_lines_song_order_idx
    on public.song_lyric_lines (song_id, language, line_index);

create table if not exists public.song_translations (
    id               uuid        primary key default gen_random_uuid(),
    song_id          uuid        not null references public.songs (id) on delete cascade,
    source_language  text        not null check (source_language in ('ja','en','es','zh')),
    target_language  text        not null check (target_language in ('ja','en','es','zh')),
    provider         text        not null default 'manual',
    alignment_report text,
    created_at       timestamptz not null default now(),
    check (source_language <> target_language),
    unique (song_id, source_language, target_language)
);
create index if not exists song_translations_song_idx
    on public.song_translations (song_id, target_language);

create table if not exists public.song_translation_lines (
    id               uuid        primary key default gen_random_uuid(),
    translation_id   uuid        not null references public.song_translations (id) on delete cascade,
    line_index       int         not null check (line_index >= 0),
    translated_text  text        not null,
    source_units     int         check (source_units is null or source_units >= 0),
    target_units     int         check (target_units is null or target_units >= 0),
    emotion          text,
    stressed         text,
    singability_note text,
    created_at       timestamptz not null default now(),
    unique (translation_id, line_index)
);
create index if not exists song_translation_lines_order_idx
    on public.song_translation_lines (translation_id, line_index);

create table if not exists public.song_vocabulary (
    id          uuid        primary key default gen_random_uuid(),
    song_id     uuid        not null references public.songs (id) on delete cascade,
    line_index  int         check (line_index is null or line_index >= 0),
    language    text        not null check (language in ('ja','en','es','zh')),
    term        text        not null,
    reading     text,
    meaning     text        not null,
    explanation text,
    created_at  timestamptz not null default now(),
    unique (song_id, language, term)
);
create index if not exists song_vocabulary_song_line_idx
    on public.song_vocabulary (song_id, line_index);

-- ============================================================================
-- vocabulary  (domain: Vocabulary) — standalone rows referenced by
-- songs.vocabulary_seed_ids and by VocabularySlide.items. Lesson.vocabulary_picks
-- are stored INLINE on the lesson (denormalized snapshot), not here.
-- ============================================================================
create table if not exists public.vocabulary (
    id         uuid        primary key default gen_random_uuid(),
    term       text        not null,   -- target language (may be CJK)
    reading    text,                   -- romanization / kana
    meaning    text        not null,
    example    text,
    created_at timestamptz not null default now()
);

-- ============================================================================
-- lessons  (domain: Lesson)
--   slide_deck       -> SlideDeck (polymorphic Slides; discriminator "type")
--   vocabulary_picks -> List<Vocabulary> stored INLINE (Vocabulary.id is nullable
--                       precisely because embedded picks need not be standalone rows)
-- ============================================================================
create table if not exists public.lessons (
    id                   uuid        primary key default gen_random_uuid(),
    song_id              uuid        not null references public.songs (id) on delete cascade,
    creator_id           uuid        references public.profiles (id) on delete set null,
    title                text        not null,
    description          text,
    language_target      text        not null,
    language_explanation text        not null,
    level                text        not null check (level in ('beginner','intermediate','advanced')),
    slide_deck           jsonb       not null default '{"slides":[]}'::jsonb,
    vocabulary_picks     jsonb       not null default '[]'::jsonb,
    discussion_prompts   text[]      not null default '{}',
    cultural_notes       text,
    is_paid              boolean     not null default false,
    price_cop            int,
    visibility           text        not null default 'public'
                           check (visibility in ('public','event_only','unlisted')),
    published_at         timestamptz,
    created_at           timestamptz not null default now()
);
create index if not exists lessons_song_id_idx    on public.lessons (song_id);
create index if not exists lessons_creator_id_idx on public.lessons (creator_id);

-- ============================================================================
-- events  (domain: Event, Build Brief §6)
-- room_id is 1:1 with the continuity Room created for the event; its FK is added
-- after rooms exists (events <-> rooms is a circular reference).
-- ============================================================================
create table if not exists public.events (
    id               uuid        primary key default gen_random_uuid(),
    title            text        not null,
    description      text,
    host_id          uuid        references public.profiles (id) on delete set null,
    starts_at        timestamptz,
    ends_at          timestamptz,
    venue_name       text,
    venue_location   text,
    luma_url         text,
    cover_image_url  text,        -- lesson-covers/ or a dedicated bucket
    featured_song_id uuid        references public.songs (id) on delete set null,  -- canción de la noche
    playlist_song_ids uuid[]     not null default '{}',  -- -> songs.id (no FK; array)
    room_id          uuid,        -- FK -> rooms(id) added below
    ticket_price_cop int,         -- null => free
    max_attendees    int,
    created_at       timestamptz not null default now()
);
create index if not exists events_host_id_idx on public.events (host_id);

-- ============================================================================
-- rooms  (domain: Room) — event_id NULL => the single global Open Room
-- ============================================================================
create table if not exists public.rooms (
    id          uuid        primary key default gen_random_uuid(),
    event_id    uuid        references public.events (id) on delete cascade,
    name        text        not null,
    description text,
    member_ids  uuid[]      not null default '{}',  -- array to match model; Open Room uses RLS
    created_at  timestamptz not null default now()
);
create index if not exists rooms_event_id_idx on public.rooms (event_id);

-- Close the events <-> rooms circle now that both tables exist.
alter table public.events
    drop constraint if exists events_room_id_fkey,
    add constraint events_room_id_fkey
        foreign key (room_id) references public.rooms (id) on delete set null;

-- ============================================================================
-- companion_matches  (domain: CompanionMatch, Build Brief §6/§8)
-- Persisted connection requests. The *suggestions* are computed by match_companion();
-- a row here is created when a user actually requests a connection.
-- ============================================================================
create table if not exists public.companion_matches (
    id                      uuid        primary key default gen_random_uuid(),
    requester_id            uuid        not null references public.profiles (id) on delete cascade,
    matched_id              uuid        not null references public.profiles (id) on delete cascade,
    score                   numeric     not null default 0,
    shared_hobbies          text[]      not null default '{}',
    complementary_languages jsonb       not null default '{}'::jsonb,
    status                  text        not null default 'pending'
                              check (status in ('pending','accepted','declined')),
    created_at              timestamptz not null default now(),
    unique (requester_id, matched_id)
);
create index if not exists companion_matches_matched_id_idx on public.companion_matches (matched_id);

-- ============================================================================
-- Helper: is_admin() — used by non-profiles policies (avoids self-recursion).
-- ============================================================================
create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.profiles
        where id = auth.uid() and 'admin' = any(roles)
    );
$$;

-- ============================================================================
-- Auto-create a profile row on signup (auth-method-agnostic: email + OAuth).
-- ============================================================================
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, display_name, avatar_url)
    values (
        new.id,
        coalesce(new.raw_user_meta_data->>'display_name',
                 new.raw_user_meta_data->>'full_name',
                 new.raw_user_meta_data->>'name',
                 split_part(new.email, '@', 1),
                 'Member'),
        coalesce(new.raw_user_meta_data->>'avatar_url',
                 new.raw_user_meta_data->>'picture')
    )
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- ============================================================================
-- match_companion(user_id)  (Build Brief §8.1) — the SHARED suggestion logic both
-- web and Android call via RPC. Intentionally crude (replace with embeddings later):
--   +3 per shared hobby, +2 per (my target ∩ their native) language.
-- Returns top 10 candidates with language complementarity. Level-adjacency and
-- same-room bonuses from the brief are TODO (need level_by_language parsing +
-- room membership) — P1, not needed for the P0 milestone.
-- ============================================================================
create or replace function public.match_companion(p_user_id uuid)
returns table (
    matched_id        uuid,
    display_name      text,
    avatar_url        text,
    target_languages  text[],
    score             numeric,
    shared_hobbies    text[]
)
language sql
stable
security definer
set search_path = public
as $$
    with me as (select * from public.profiles where id = p_user_id)
    select
        c.id,
        c.display_name,
        c.avatar_url,
        c.target_languages,
        ( 3 * coalesce(array_length(array(
              select unnest(c.hobbies) intersect select unnest(me.hobbies)), 1), 0)
        + 2 * coalesce(array_length(array(
              select unnest(c.native_languages) intersect select unnest(me.target_languages)), 1), 0)
        )::numeric as score,
        array(select unnest(c.hobbies) intersect select unnest(me.hobbies)) as shared_hobbies
    from public.profiles c, me
    where c.id <> p_user_id
      and (
          array(select unnest(me.target_languages) intersect select unnest(c.native_languages)) <> '{}'
          or array(select unnest(me.native_languages) intersect select unnest(c.target_languages)) <> '{}'
      )
    order by score desc
    limit 10;
$$;

-- ============================================================================
-- Row Level Security
-- ============================================================================
alter table public.profiles          enable row level security;
alter table public.songs             enable row level security;
alter table public.song_sources      enable row level security;
alter table public.song_lyric_lines  enable row level security;
alter table public.song_translations enable row level security;
alter table public.song_translation_lines enable row level security;
alter table public.song_vocabulary   enable row level security;
alter table public.vocabulary        enable row level security;
alter table public.lessons           enable row level security;
alter table public.events            enable row level security;
alter table public.rooms             enable row level security;
alter table public.companion_matches enable row level security;

-- Explicit privileges make the contract independent of dashboard default grants.
-- RLS below still decides which rows each role can access.
grant select on public.songs,
    public.song_lyric_lines,
    public.song_translations,
    public.song_translation_lines,
    public.song_vocabulary
to anon, authenticated;

grant insert, update, delete on public.songs,
    public.song_lyric_lines,
    public.song_translations,
    public.song_translation_lines,
    public.song_vocabulary
to authenticated;

grant select, insert, update, delete on public.song_sources to authenticated;

-- ---- profiles --------------------------------------------------------------
-- Authenticated-only read (decision: profiles are not public). Companion matching
-- still works because it runs for signed-in users. For public creator/host pages
-- later, add a policy `to anon` scoped to a `is_public` column.
drop policy if exists profiles_select on public.profiles;
create policy profiles_select on public.profiles
    for select to authenticated using (true);
drop policy if exists profiles_insert_self on public.profiles;
create policy profiles_insert_self on public.profiles
    for insert to authenticated with check (auth.uid() = id);
drop policy if exists profiles_update_self on public.profiles;
create policy profiles_update_self on public.profiles
    for update to authenticated using (auth.uid() = id) with check (auth.uid() = id);

-- ---- songs (catalog: read all authed; write admin-only) --------------------
drop policy if exists songs_select on public.songs;
create policy songs_select on public.songs
    for select to authenticated using (true);
drop policy if exists songs_public_select on public.songs;
create policy songs_public_select on public.songs
    for select to anon using (is_public_domain);
drop policy if exists songs_write on public.songs;
create policy songs_write on public.songs
    for all to authenticated using (public.is_admin()) with check (public.is_admin());

-- song_sources are written by the resolver Edge Function with service-role.
-- Admin access remains available for auditing and manual correction.
drop policy if exists song_sources_admin on public.song_sources;
create policy song_sources_admin on public.song_sources
    for all to authenticated using (public.is_admin()) with check (public.is_admin());

-- ---- translated-song data --------------------------------------------------
-- Anonymous users see child rows only when their parent song is public-domain.
drop policy if exists song_lyric_lines_select on public.song_lyric_lines;
create policy song_lyric_lines_select on public.song_lyric_lines
    for select to authenticated using (true);
drop policy if exists song_lyric_lines_public_select on public.song_lyric_lines;
create policy song_lyric_lines_public_select on public.song_lyric_lines
    for select to anon using (exists (
        select 1 from public.songs s
        where s.id = song_lyric_lines.song_id and s.is_public_domain));
drop policy if exists song_lyric_lines_write on public.song_lyric_lines;
create policy song_lyric_lines_write on public.song_lyric_lines
    for all to authenticated using (public.is_admin()) with check (public.is_admin());

drop policy if exists song_translations_select on public.song_translations;
create policy song_translations_select on public.song_translations
    for select to authenticated using (true);
drop policy if exists song_translations_public_select on public.song_translations;
create policy song_translations_public_select on public.song_translations
    for select to anon using (exists (
        select 1 from public.songs s
        where s.id = song_translations.song_id and s.is_public_domain));
drop policy if exists song_translations_write on public.song_translations;
create policy song_translations_write on public.song_translations
    for all to authenticated using (public.is_admin()) with check (public.is_admin());

drop policy if exists song_translation_lines_select on public.song_translation_lines;
create policy song_translation_lines_select on public.song_translation_lines
    for select to authenticated using (true);
drop policy if exists song_translation_lines_public_select on public.song_translation_lines;
create policy song_translation_lines_public_select on public.song_translation_lines
    for select to anon using (exists (
        select 1
        from public.song_translations t
        join public.songs s on s.id = t.song_id
        where t.id = song_translation_lines.translation_id and s.is_public_domain));
drop policy if exists song_translation_lines_write on public.song_translation_lines;
create policy song_translation_lines_write on public.song_translation_lines
    for all to authenticated using (public.is_admin()) with check (public.is_admin());

drop policy if exists song_vocabulary_select on public.song_vocabulary;
create policy song_vocabulary_select on public.song_vocabulary
    for select to authenticated using (true);
drop policy if exists song_vocabulary_public_select on public.song_vocabulary;
create policy song_vocabulary_public_select on public.song_vocabulary
    for select to anon using (exists (
        select 1 from public.songs s
        where s.id = song_vocabulary.song_id and s.is_public_domain));
drop policy if exists song_vocabulary_write on public.song_vocabulary;
create policy song_vocabulary_write on public.song_vocabulary
    for all to authenticated using (public.is_admin()) with check (public.is_admin());

-- ---- vocabulary (read all authed; write creators + admins) -----------------
drop policy if exists vocabulary_select on public.vocabulary;
create policy vocabulary_select on public.vocabulary
    for select to authenticated using (true);
drop policy if exists vocabulary_write on public.vocabulary;
create policy vocabulary_write on public.vocabulary
    for all to authenticated
    using (public.is_admin() or exists (
        select 1 from public.profiles p where p.id = auth.uid() and 'creator' = any(p.roles)))
    with check (public.is_admin() or exists (
        select 1 from public.profiles p where p.id = auth.uid() and 'creator' = any(p.roles)));

-- ---- lessons ---------------------------------------------------------------
-- Read: published public, OR unlisted (reachable by id), OR your own, OR admin,
-- OR event_only when you're a member of a room tied to an event whose featured
-- song is this lesson's song (i.e. you attended the night this lesson is for).
drop policy if exists lessons_select on public.lessons;
create policy lessons_select on public.lessons
    for select to authenticated using (
        (visibility = 'public' and published_at is not null)
        or visibility = 'unlisted'
        or creator_id = auth.uid()
        or public.is_admin()
        or (visibility = 'event_only' and exists (
                select 1
                from public.rooms r
                join public.events e on e.id = r.event_id
                where e.featured_song_id = lessons.song_id
                  and auth.uid() = any(r.member_ids)
        ))
    );
drop policy if exists lessons_write on public.lessons;
create policy lessons_write on public.lessons
    for all to authenticated
    using (creator_id = auth.uid() or public.is_admin())
    with check (creator_id = auth.uid() or public.is_admin());

-- ---- events ----------------------------------------------------------------
-- Read for all authed (event landing pages). Write: host of the event or admin.
drop policy if exists events_select on public.events;
create policy events_select on public.events
    for select to authenticated using (true);
drop policy if exists events_write on public.events;
create policy events_write on public.events
    for all to authenticated
    using (host_id = auth.uid() or public.is_admin())
    with check (host_id = auth.uid() or public.is_admin());

-- ---- rooms -----------------------------------------------------------------
-- Read: the global Open Room (event_id null) is visible to every authed user;
-- event rooms are visible to their members. Writes admin/host-managed.
drop policy if exists rooms_select on public.rooms;
create policy rooms_select on public.rooms
    for select to authenticated using (
        event_id is null
        or auth.uid() = any(member_ids)
        or public.is_admin()
    );
drop policy if exists rooms_write on public.rooms;
create policy rooms_write on public.rooms
    for all to authenticated using (public.is_admin()) with check (public.is_admin());

-- ---- companion_matches -----------------------------------------------------
-- You can see matches you sent or received. You create only requests you send.
-- The matched user can update status (accept/decline); the requester can too
-- (e.g. cancel). Deletes admin-only.
drop policy if exists companion_select on public.companion_matches;
create policy companion_select on public.companion_matches
    for select to authenticated using (
        requester_id = auth.uid() or matched_id = auth.uid() or public.is_admin()
    );
drop policy if exists companion_insert on public.companion_matches;
create policy companion_insert on public.companion_matches
    for insert to authenticated with check (requester_id = auth.uid());
drop policy if exists companion_update on public.companion_matches;
create policy companion_update on public.companion_matches
    for update to authenticated
    using (requester_id = auth.uid() or matched_id = auth.uid())
    with check (requester_id = auth.uid() or matched_id = auth.uid());

-- ============================================================================
-- SEED (optional): the single global Open Room (Build Brief §10 P0 #6).
-- Every authenticated user can read it via RLS; no membership row needed.
-- ============================================================================
insert into public.rooms (id, event_id, name, description)
values (
    '00000000-0000-0000-0000-000000000001',
    null,
    'Open Room',
    'The global room every Jikanle member shares. Say hi.'
)
on conflict (id) do nothing;
