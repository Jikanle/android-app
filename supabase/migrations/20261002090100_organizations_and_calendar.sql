-- ============================================================================
-- Organizations + institution-host calendar (Plan Part C).
-- Additive + idempotent. Run in the Supabase SQL editor after schema.sql.
--
-- Turns `events` into a Luma-style calendar owned by institutions: each org
-- (UNAL, Confucio, Centro del Japón, BLAA Sala de Idiomas, Jikanle) has editors
-- who manage only THEIR events. Public events are readable logged-out for
-- discovery; RSVP/premium still require auth.
-- ============================================================================

-- ---- organizations ---------------------------------------------------------
create table if not exists public.organizations (
    id         uuid        primary key default gen_random_uuid(),
    name       text        not null,
    slug       text        not null unique,
    logo_url   text,
    kind       text        not null default 'institute'
                             check (kind in ('university','institute','library','jikanle')),
    website    text,
    created_at timestamptz not null default now()
);

-- who may host for an organization (Alfredo @ BLAA, Jenny @ UNAL, Confucio designees…)
create table if not exists public.organization_members (
    organization_id uuid not null references public.organizations (id) on delete cascade,
    profile_id      uuid not null references public.profiles (id) on delete cascade,
    role            text not null default 'editor' check (role in ('owner','editor')),
    created_at      timestamptz not null default now(),
    primary key (organization_id, profile_id)
);
create index if not exists organization_members_profile_idx
    on public.organization_members (profile_id);

-- ---- extend events into calendar rows (idempotent) -------------------------
alter table public.events
    add column if not exists organization_id uuid references public.organizations (id) on delete set null,
    add column if not exists language        text,
    add column if not exists level           text,
    add column if not exists summary         text,
    add column if not exists tags            text[] not null default '{}',
    add column if not exists timezone        text,
    add column if not exists recurrence      text,          -- RRULE for monthly schedules
    add column if not exists lat             double precision,
    add column if not exists lng             double precision,
    add column if not exists visibility      text not null default 'public'
                                             check (visibility in ('public','unlisted','private'));
create index if not exists events_org_starts_idx on public.events (organization_id, starts_at);
create index if not exists events_public_starts_idx on public.events (starts_at) where visibility = 'public';

-- ---- helper: may the current user edit this org's events? ------------------
create or replace function public.is_org_editor(p_org uuid)
returns boolean language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from public.organization_members m
        where m.organization_id = p_org and m.profile_id = auth.uid()
    );
$$;

-- ---- RLS -------------------------------------------------------------------
alter table public.organizations       enable row level security;
alter table public.organization_members enable row level security;

-- Organizations are public (needed for logged-out event cards); writes admin-only.
drop policy if exists organizations_read_all on public.organizations;
create policy organizations_read_all on public.organizations
    for select to anon, authenticated using (true);
drop policy if exists organizations_admin_write on public.organizations;
create policy organizations_admin_write on public.organizations
    for all to authenticated using (public.is_admin()) with check (public.is_admin());

-- Members: a user sees their own memberships; org owners + admins manage rosters.
drop policy if exists org_members_read on public.organization_members;
create policy org_members_read on public.organization_members
    for select to authenticated
    using (profile_id = auth.uid() or public.is_admin() or public.is_org_editor(organization_id));
drop policy if exists org_members_admin_write on public.organization_members;
create policy org_members_admin_write on public.organization_members
    for all to authenticated using (public.is_admin()) with check (public.is_admin());

-- Events: allow logged-OUT discovery of public events (schema.sql already has an
-- authenticated select + a host/admin write policy; we add anon read + org-editor write).
drop policy if exists events_select_public_anon on public.events;
create policy events_select_public_anon on public.events
    for select to anon using (visibility = 'public');

drop policy if exists events_write on public.events;
create policy events_write on public.events
    for all to authenticated
    using (host_id = auth.uid() or public.is_admin() or public.is_org_editor(organization_id))
    with check (host_id = auth.uid() or public.is_admin() or public.is_org_editor(organization_id));

-- ---- seed the real host institutions (idempotent) --------------------------
insert into public.organizations (id, name, slug, kind, website) values
    ('10000000-0000-4000-8000-000000000001', 'Jikanle',                              'jikanle',              'jikanle',    'https://jikanle.com.co'),
    ('10000000-0000-4000-8000-000000000002', 'Universidad Nacional de Colombia',     'unal',                 'university', null),
    ('10000000-0000-4000-8000-000000000003', 'Instituto Confucio — Uniandes',        'confucio-uniandes',    'institute',  null),
    ('10000000-0000-4000-8000-000000000004', 'Instituto Confucio — U. Tadeo',        'confucio-tadeo',       'institute',  null),
    ('10000000-0000-4000-8000-000000000005', 'Centro del Japón — Uniandes',          'centro-japon-uniandes','institute',  null),
    ('10000000-0000-4000-8000-000000000006', 'Sala de Idiomas — BLAA',               'blaa-sala-idiomas',    'library',    null)
on conflict (id) do nothing;
