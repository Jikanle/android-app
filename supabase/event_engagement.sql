-- PROPOSED additive shared contract; review ADR 0004 before staging application.
-- Requires schema.sql and organizations_and_calendar.sql. No scraping or deployment.
begin;
create table if not exists public.event_preferences (
    user_id uuid primary key references auth.users(id) on delete cascade,
    languages text[] not null default '{}' check (
        languages <@ array['ja','pt','es','en','ko','zh']::text[]),
    interests text[] not null default '{}' check (cardinality(interests) <= 12 and length(interests::text) <= 1000),
    professional_domains text[] not null default '{}' check (cardinality(professional_domains) <= 8 and length(professional_domains::text) <= 600),
    goals text[] not null default '{}' check (goals <@ array['conversation','culture','music','professional_exchange']::text[]),
    matching_opt_in boolean not null default false,
    metrics_opt_in boolean not null default false,
    contact_opt_in boolean not null default false,
    consent_version text not null check (length(consent_version) between 1 and 64),
    consent_recorded_at timestamptz not null default now()
);
-- The database records consent changes; do not trust a client timestamp as evidence.
create or replace function public.stamp_event_preferences()
returns trigger language plpgsql set search_path = public as $$
begin
    new.consent_recorded_at := now();
    return new;
end;
$$;
drop trigger if exists stamp_event_preferences on public.event_preferences;
create trigger stamp_event_preferences before insert or update on public.event_preferences
    for each row execute function public.stamp_event_preferences();
alter table public.event_preferences enable row level security;
revoke all on public.event_preferences from anon, authenticated;
grant select, insert, update, delete on public.event_preferences to authenticated;
drop policy if exists event_preferences_own on public.event_preferences;
create policy event_preferences_own on public.event_preferences for all to authenticated
    using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);

-- One participant per actual event occurrence, not per recurring series.
-- Trusted operator/service only writes after verified RSVP import/check-in.
create table if not exists public.event_participations (
    event_id uuid not null references public.events(id) on delete cascade,
    user_id uuid not null references auth.users(id) on delete cascade,
    rsvp_at timestamptz,
    checked_in_at timestamptz,
    verification_method text check (verification_method in ('host_roster','host_qr')),
    analytics_allowed boolean not null default false,
    consent_version text,
    recorded_at timestamptz not null default now(),
    primary key (event_id, user_id),
    check (rsvp_at is not null or checked_in_at is not null),
    check ((checked_in_at is null and verification_method is null)
        or (checked_in_at is not null and verification_method is not null)),
    check (not analytics_allowed or (consent_version is not null and length(consent_version) between 1 and 64))
);
alter table public.event_participations enable row level security;
revoke all on public.event_participations from anon, authenticated;
grant select on public.event_participations to authenticated;
grant all on public.event_preferences, public.event_participations to service_role;
drop policy if exists event_participations_read_own on public.event_participations;
create policy event_participations_read_own on public.event_participations for select to authenticated
    using ((select auth.uid()) = user_id);
create index if not exists event_participations_user_idx on public.event_participations(user_id, event_id);
commit;
