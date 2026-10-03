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
-- Event-specific purposes only. Web owns account_preferences and marketing channels.
create table if not exists public.event_consent_history (
    id bigint generated always as identity primary key,
    user_id uuid not null references auth.users(id) on delete cascade,
    metrics_opt_in boolean not null,
    matching_opt_in boolean not null,
    consent_version text not null,
    recorded_at timestamptz not null default clock_timestamp()
);
alter table public.event_consent_history enable row level security;
revoke all on public.event_consent_history from anon, authenticated;
grant select on public.event_consent_history to authenticated;
drop policy if exists event_consent_read_own on public.event_consent_history;
create policy event_consent_read_own on public.event_consent_history for select to authenticated
    using ((select auth.uid()) = user_id);
create index if not exists event_consent_user_time on public.event_consent_history(user_id, recorded_at desc, id desc);

create or replace function public.record_event_consent()
returns trigger language plpgsql security definer set search_path = public, pg_temp as $$
begin
    if TG_OP = 'DELETE' then
        insert into public.event_consent_history(user_id,metrics_opt_in,matching_opt_in,consent_version)
            values(old.user_id,false,false,old.consent_version);
        update public.event_participations set analytics_allowed = false where user_id = old.user_id;
        return old;
    end if;
    if TG_OP = 'INSERT' or
       (new.metrics_opt_in,new.matching_opt_in,new.consent_version) is distinct from
       (old.metrics_opt_in,old.matching_opt_in,old.consent_version) then
        insert into public.event_consent_history(user_id,metrics_opt_in,matching_opt_in,consent_version)
            values(new.user_id,new.metrics_opt_in,new.matching_opt_in,new.consent_version);
    end if;
    if not new.metrics_opt_in then
        update public.event_participations set analytics_allowed = false where user_id = new.user_id;
    end if;
    return new;
end;
$$;
drop trigger if exists record_event_consent on public.event_preferences;
create trigger record_event_consent after insert or update or delete on public.event_preferences
    for each row execute function public.record_event_consent();

-- All writes go through validated RPCs; clients cannot invent policy versions/times.
revoke insert, update, delete on public.event_preferences from authenticated;
create or replace function public.save_event_preferences(p_languages text[], p_interests text[],
    p_domains text[], p_goals text[], p_matching boolean, p_metrics boolean)
returns void language plpgsql security definer set search_path = public, pg_temp as $$
begin
    if auth.uid() is null then raise exception 'Authentication required'; end if;
    if p_languages is null or p_interests is null or p_domains is null or p_goals is null
       or p_matching is null or p_metrics is null
       or array_position(p_languages,null) is not null or array_position(p_interests,null) is not null
       or array_position(p_domains,null) is not null or array_position(p_goals,null) is not null
       or not p_interests <@ array['music','culture','technology','literature']::text[]
       or not p_domains <@ array['arts','education','engineering','research']::text[] then
        raise exception 'Invalid preference tags';
    end if;
    insert into public.event_preferences(user_id,languages,interests,professional_domains,goals,
        matching_opt_in,metrics_opt_in,contact_opt_in,consent_version)
    values(auth.uid(),p_languages,p_interests,p_domains,p_goals,p_matching,p_metrics,false,'events-v1')
    on conflict(user_id) do update set languages=excluded.languages, interests=excluded.interests,
        professional_domains=excluded.professional_domains, goals=excluded.goals,
        matching_opt_in=excluded.matching_opt_in,metrics_opt_in=excluded.metrics_opt_in,
        contact_opt_in=false,consent_version=excluded.consent_version;
end;
$$;
create or replace function public.withdraw_event_preferences()
returns void language plpgsql security definer set search_path = public, pg_temp as $$
begin
    if auth.uid() is null then raise exception 'Authentication required'; end if;
    -- Lock order is the same as the importer. Delete tags but retain a denied state.
    perform public.save_event_preferences('{}','{}','{}','{}',false,false);
end;
$$;
revoke all on function public.save_event_preferences(text[],text[],text[],text[],boolean,boolean),
    public.withdraw_event_preferences() from public,anon;
grant execute on function public.save_event_preferences(text[],text[],text[],text[],boolean,boolean),
    public.withdraw_event_preferences() to authenticated;

alter table public.event_participations
    add column if not exists verified_by uuid references auth.users(id) on delete set null,
    add column if not exists evidence_ref text;

-- Authenticated host RPC, not a service-role key in a browser or Android client.
create or replace function public.import_verified_attendance(p_event_id uuid, p_rows jsonb, p_evidence_ref text)
returns jsonb language plpgsql security definer set search_path = public, pg_temp as $$
declare
    e public.events;
    item jsonb;
    participant uuid;
    observed timestamptz;
    pref public.event_preferences;
    permission public.event_consent_history;
    inserted integer := 0;
    affected integer;
begin
    if auth.uid() is null then raise exception 'Authentication required'; end if;
    select * into e from public.events where id=p_event_id;
    if not found then raise exception 'Event unavailable'; end if;
    if not (coalesce(e.host_id=auth.uid(),false) or exists (
        select 1 from public.organization_members m
        where m.organization_id=e.organization_id and m.profile_id=auth.uid())) then
        raise exception 'Only this event host or organization editor may verify attendance';
    end if;
    if jsonb_typeof(p_rows) is distinct from 'array' then raise exception 'Expected roster array'; end if;
    if jsonb_array_length(p_rows) not between 1 and 100 or p_evidence_ref is null
       or p_evidence_ref !~ '^roster:[a-zA-Z0-9_-]{1,64}$' then raise exception 'Invalid batch'; end if;
    if (select count(distinct value->>'user_id') from jsonb_array_elements(p_rows)) <> jsonb_array_length(p_rows)
        then raise exception 'Duplicate or missing participant'; end if;
    for item in select value from jsonb_array_elements(p_rows) order by value->>'user_id' loop
        if jsonb_typeof(item) <> 'object' or item - array['user_id','checked_in_at'] <> '{}'::jsonb
           or jsonb_typeof(item->'user_id') is distinct from 'string'
           or jsonb_typeof(item->'checked_in_at') is distinct from 'string'
           or (item->>'checked_in_at') !~ 'T.*(Z|[+-][0-9]{2}:[0-9]{2})$' then
            raise exception 'Expected user_id and offset-aware checked_in_at only';
        end if;
        participant := (item->>'user_id')::uuid;
        observed := (item->>'checked_in_at')::timestamptz;
        if e.starts_at is null or not isfinite(observed) or observed > clock_timestamp()
           or observed < e.starts_at - interval '2 hours'
           or observed > coalesce(e.ends_at,e.starts_at+interval '8 hours') + interval '12 hours' then
            raise exception 'Check-in outside event window';
        end if;
        select * into pref from public.event_preferences where user_id=participant for update;
        select * into permission from public.event_consent_history
            where user_id=participant and recorded_at <= observed order by recorded_at desc,id desc limit 1;
        insert into public.event_participations(event_id,user_id,checked_in_at,verification_method,
            analytics_allowed,consent_version,verified_by,evidence_ref)
        values(p_event_id,participant,observed,'host_roster',
            coalesce(pref.metrics_opt_in and permission.metrics_opt_in,false) and not exists (
                select 1 from public.event_consent_history h where h.user_id=participant
                and h.recorded_at > observed and not h.metrics_opt_in),
            permission.consent_version,auth.uid(),p_evidence_ref)
        on conflict(event_id,user_id) do update set checked_in_at=excluded.checked_in_at,
            verification_method=excluded.verification_method,analytics_allowed=excluded.analytics_allowed,
            consent_version=excluded.consent_version,verified_by=excluded.verified_by,evidence_ref=excluded.evidence_ref
            where event_participations.checked_in_at is null;
        get diagnostics affected = row_count;
        inserted := inserted + affected;
    end loop;
    return jsonb_build_object('verified',inserted,'unchanged',jsonb_array_length(p_rows)-inserted);
end;
$$;
revoke all on function public.import_verified_attendance(uuid,jsonb,text) from public,anon;
grant execute on function public.import_verified_attendance(uuid,jsonb,text) to authenticated;
commit;
