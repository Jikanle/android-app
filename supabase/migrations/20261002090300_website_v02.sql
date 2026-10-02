-- Website v0.2, updated 2026-09-22. Shared Web/Android contract; not applied automatically.
-- Apply AFTER schema.sql, organizations_and_calendar.sql, organization_invites.sql.
-- Re-running is safe. Do not re-run older policy scripts after this migration.
begin;

-- Self-service profiles must never grant administrative capabilities.
create or replace function public.protect_profile_privileges()
returns trigger language plpgsql set search_path = public as $$
begin
  if current_user in ('anon', 'authenticated') and not public.is_admin() then
    if tg_op = 'INSERT' and new.roles <> array['learner']::text[] then
      raise exception 'Profile roles are managed by administrators';
    elsif tg_op = 'UPDATE' and (new.roles is distinct from old.roles or new.id <> old.id) then
      raise exception 'Profile identity and roles are managed by administrators';
    end if;
  end if;
  return new;
end $$;
drop trigger if exists protect_profile_privileges on public.profiles;
create trigger protect_profile_privileges before insert or update on public.profiles
for each row execute function public.protect_profile_privileges();

create table if not exists public.communities (
  id uuid primary key default gen_random_uuid(),
  organization_id uuid not null references public.organizations(id),
  name text not null check (length(name) between 1 and 150),
  slug text not null check (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
  description text not null default '' check (length(description) <= 4000),
  languages text[] not null default '{}' check (languages <@ array['ja','zh','ko','es','pt','en']),
  city text not null default 'Bogotá' check (length(city) between 1 and 100),
  format text not null default 'in_person' check (format in ('in_person','online','hybrid')),
  logo_url text, cover_image_url text, usual_meeting_location text,
  meeting_frequency text, schedule_summary text, audience text, website text,
  -- Only a public group invitation; personal phone-based WhatsApp URLs are forbidden.
  public_channel_url text check (public_channel_url is null or public_channel_url ~ '^https://(chat\.whatsapp\.com/[A-Za-z0-9_-]+|t\.me/[A-Za-z_][A-Za-z0-9_+/-]*|discord\.gg/[A-Za-z0-9-]+|discord\.com/invite/[A-Za-z0-9-]+)$'),
  relationship text not null default 'listed' check (relationship in ('listed','claimed','identity_verified','official_ally','jikanle_operated')),
  status text not null default 'draft' check (status in ('draft','submitted','under_review','approved','rejected','suspended','archived')),
  last_verified_at timestamptz, created_at timestamptz not null default now(),
  unique(city, slug), unique(id, organization_id)
);
alter table public.communities enable row level security;
grant select on public.communities to anon, authenticated;
grant insert, update, delete on public.communities to authenticated;
drop policy if exists communities_public on public.communities;
create policy communities_public on public.communities for select to anon, authenticated using (status = 'approved');
drop policy if exists communities_leader_read on public.communities;
create policy communities_leader_read on public.communities for select to authenticated using (public.is_org_editor(organization_id) or public.is_admin());
drop policy if exists communities_leader_update on public.communities;
create policy communities_leader_update on public.communities for update to authenticated
using (public.is_org_editor(organization_id) and status not in ('suspended','archived'))
with check (public.is_org_editor(organization_id) and status not in ('suspended','archived'));
drop policy if exists communities_admin on public.communities;
create policy communities_admin on public.communities for all to authenticated using (public.is_admin()) with check (public.is_admin());
create or replace function public.protect_community_trust()
returns trigger language plpgsql set search_path = public as $$
begin
  if current_user in ('anon','authenticated') and not public.is_admin() and
    (new.id <> old.id or new.organization_id <> old.organization_id or new.slug <> old.slug or
     new.status <> old.status or new.relationship <> old.relationship or
     new.last_verified_at is distinct from old.last_verified_at) then
    raise exception 'Community ownership, publication and verification are administrator controlled';
  end if;
  return new;
end $$;
drop trigger if exists protect_community_trust on public.communities;
create trigger protect_community_trust before update on public.communities
for each row execute function public.protect_community_trust();

-- Existing published calendar entries remain published; NEW entries default to draft.
alter table public.events
  add column if not exists slug text,
  add column if not exists city text not null default 'Bogotá',
  add column if not exists languages text[] not null default '{}',
  add column if not exists status text not null default 'published' check (status in ('draft','published','cancelled','completed')),
  add column if not exists event_type text not null default 'community_event',
  add column if not exists format text not null default 'in_person' check (format in ('in_person','online','hybrid')),
  add column if not exists participation_style text,
  add column if not exists community_id uuid references public.communities(id),
  add column if not exists registration_provider text check (registration_provider in ('jikanle','luma','eventbrite','external','none')),
  add column if not exists registration_url text,
  add column if not exists source_url text,
  add column if not exists source_type text,
  add column if not exists learning_objectives text[] not null default '{}',
  add column if not exists accessibility_notes text,
  add column if not exists contact_method text,
  add column if not exists last_verified_at timestamptz,
  add column if not exists age_restriction text,
  add column if not exists alcohol_present boolean,
  add column if not exists attendee_count integer check (attendee_count >= 0),
  add column if not exists featured boolean not null default false;
alter table public.events alter column status set default 'draft';
create unique index if not exists events_slug_unique on public.events(slug) where slug is not null;
create index if not exists events_community_start on public.events(community_id, starts_at);
create or replace function public.protect_event_relationship()
returns trigger language plpgsql set search_path = public as $$
begin
  if new.community_id is not null and not exists (
    select 1 from public.communities c where c.id = new.community_id and c.organization_id = new.organization_id
  ) then raise exception 'Event community must belong to its organization'; end if;
  if current_user in ('anon','authenticated') and not public.is_admin() then
    if (tg_op = 'INSERT' and (new.featured or new.last_verified_at is not null)) or
       (tg_op = 'UPDATE' and (new.featured <> old.featured or new.last_verified_at is distinct from old.last_verified_at)) then
      raise exception 'Featuring and verification are administrator controlled';
    end if;
  end if;
  return new;
end $$;
drop trigger if exists protect_event_relationship on public.events;
create trigger protect_event_relationship before insert or update on public.events
for each row execute function public.protect_event_relationship();
drop policy if exists events_select on public.events;
drop policy if exists events_select_public_anon on public.events;
drop policy if exists events_write on public.events;
drop policy if exists events_v02_public on public.events;
create policy events_v02_public on public.events for select to anon, authenticated
using (visibility = 'public' and status in ('published','completed','cancelled'));
drop policy if exists events_v02_manage on public.events;
create policy events_v02_manage on public.events for all to authenticated
using (public.is_admin() or public.is_org_editor(organization_id) or (organization_id is null and host_id = auth.uid()))
with check (public.is_admin() or public.is_org_editor(organization_id) or (organization_id is null and host_id = auth.uid()));
grant select on public.events to anon, authenticated;
grant insert, update, delete on public.events to authenticated;

create table if not exists public.account_preferences (
  user_id uuid primary key references public.profiles(id) on delete cascade,
  display_name text not null check (length(trim(display_name)) between 1 and 100),
  city text not null check (length(trim(city)) between 1 and 100),
  target_languages text[] not null check (cardinality(target_languages) between 1 and 5 and target_languages <@ array['ja','zh','ko','es','pt']),
  native_languages text[] not null default '{}' check (native_languages <@ array['ja','zh','ko','es','pt','en']),
  platform_interest text not null check (platform_interest in ('android','web','ios')),
  primary_goal text not null check (primary_goal in ('learn','events','community','professional','research','musician','educator')),
  phone text,
  email_marketing boolean not null default false,
  whatsapp_marketing boolean not null default false,
  privacy_acknowledged boolean not null check (privacy_acknowledged),
  locale text not null default 'es' check (locale in ('es','ja','zh','ko')),
  consent_version text not null check (length(consent_version) between 1 and 100),
  updated_at timestamptz not null default now(),
  check ((not whatsapp_marketing and phone is null) or (whatsapp_marketing and phone is not null and phone ~ '^\+?[0-9 ()-]{7,30}$'))
);
alter table public.account_preferences enable row level security;
grant select, insert, update, delete on public.account_preferences to authenticated;
drop policy if exists preferences_owner on public.account_preferences;
create policy preferences_owner on public.account_preferences for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create table if not exists public.account_consent_history (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  email_marketing boolean not null, whatsapp_marketing boolean not null,
  consent_version text not null, recorded_at timestamptz not null default now()
);
alter table public.account_consent_history enable row level security;
revoke all on public.account_consent_history from anon, authenticated;
grant select on public.account_consent_history to authenticated;
drop policy if exists consent_owner_read on public.account_consent_history;
create policy consent_owner_read on public.account_consent_history for select to authenticated using (user_id = auth.uid());
create or replace function public.record_account_consent()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  if tg_op = 'INSERT' or new.email_marketing is distinct from old.email_marketing or
     new.whatsapp_marketing is distinct from old.whatsapp_marketing or new.consent_version <> old.consent_version then
    insert into public.account_consent_history(user_id,email_marketing,whatsapp_marketing,consent_version)
    values(new.user_id,new.email_marketing,new.whatsapp_marketing,new.consent_version);
  end if;
  return new;
end $$;
drop trigger if exists record_account_consent on public.account_preferences;
create trigger record_account_consent after insert or update on public.account_preferences
for each row execute function public.record_account_consent();
create or replace function public.complete_website_onboarding(p_preferences jsonb)
returns void language plpgsql security invoker set search_path = public as $$
declare p public.account_preferences;
begin
  if auth.uid() is null then raise exception 'Authentication required'; end if;
  p = jsonb_populate_record(null::public.account_preferences, p_preferences);
  insert into public.account_preferences(user_id,display_name,city,target_languages,native_languages,platform_interest,primary_goal,phone,email_marketing,whatsapp_marketing,privacy_acknowledged,locale,consent_version)
  values(auth.uid(),p.display_name,p.city,p.target_languages,coalesce(p.native_languages,'{}'),p.platform_interest,p.primary_goal,case when p.whatsapp_marketing then p.phone else null end,p.email_marketing,p.whatsapp_marketing,p.privacy_acknowledged,p.locale,p.consent_version)
  on conflict(user_id) do update set display_name=excluded.display_name,city=excluded.city,target_languages=excluded.target_languages,
    native_languages=excluded.native_languages,platform_interest=excluded.platform_interest,primary_goal=excluded.primary_goal,
    phone=excluded.phone,email_marketing=excluded.email_marketing,whatsapp_marketing=excluded.whatsapp_marketing,
    privacy_acknowledged=excluded.privacy_acknowledged,locale=excluded.locale,consent_version=excluded.consent_version,updated_at=now();
  update public.profiles set display_name=p.display_name where id=auth.uid();
end $$;
revoke all on function public.complete_website_onboarding(jsonb) from public, anon;
grant execute on function public.complete_website_onboarding(jsonb) to authenticated;

create table if not exists public.community_follows (
  community_id uuid not null references public.communities(id) on delete cascade,
  user_id uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now(), primary key(community_id,user_id)
);
alter table public.community_follows enable row level security;
grant select, insert, delete on public.community_follows to authenticated;
drop policy if exists follows_owner_read on public.community_follows;
create policy follows_owner_read on public.community_follows for select to authenticated using (user_id=auth.uid());
drop policy if exists follows_owner_insert on public.community_follows;
create policy follows_owner_insert on public.community_follows for insert to authenticated
with check (user_id=auth.uid() and exists(select 1 from public.communities where id=community_id and status='approved'));
drop policy if exists follows_owner_delete on public.community_follows;
create policy follows_owner_delete on public.community_follows for delete to authenticated using (user_id=auth.uid());
create or replace function public.community_follower_count(p_community uuid)
returns bigint language sql stable security definer set search_path = public as $$
  select count(*) from public.community_follows f where f.community_id=p_community
    and exists(select 1 from public.communities c where c.id=p_community and c.status='approved');
$$;
revoke all on function public.community_follower_count(uuid) from public;
grant execute on function public.community_follower_count(uuid) to anon, authenticated;

create table if not exists public.interest_requests (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references public.profiles(id) on delete set null,
  kind text not null check (kind in ('developer','partner','talent','collaborator','course','contact')),
  name text not null check (length(trim(name)) between 1 and 100),
  email text not null check (length(email) <= 254 and email ~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'),
  message text not null check (length(message) between 10 and 4000),
  route text not null check (left(route,1)='/' and length(route)<=300),
  contact_permission boolean not null check (contact_permission),
  consent_version text not null, created_at timestamptz not null default now()
);
create table if not exists public.feedback (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references public.profiles(id) on delete set null,
  category text not null check (category in ('translation','event_information','community_information','bug','accessibility','safety','account','payment','other')),
  message text not null check (length(message) between 10 and 4000),
  route text not null check (left(route,1)='/' and length(route)<=300),
  language text check (language in ('ja','zh','ko','es','pt','en')),
  locale text not null check (locale in ('es','ja','zh','ko')),
  contact_permission boolean not null default false, contact_email text,
  translation_provider text, translation_version text,
  status text not null default 'submitted' check (status in ('submitted','reviewing','resolved')),
  created_at timestamptz not null default now(),
  check ((not contact_permission and contact_email is null) or (contact_permission and contact_email is not null and length(contact_email)<=254 and contact_email ~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'))
);
alter table public.interest_requests enable row level security;
alter table public.feedback enable row level security;
revoke all on public.interest_requests,public.feedback from anon, authenticated;
grant insert on public.interest_requests,public.feedback to anon, authenticated;
grant select, update, delete on public.interest_requests,public.feedback to authenticated;
drop policy if exists interest_submit on public.interest_requests;
create policy interest_submit on public.interest_requests for insert to anon, authenticated with check (user_id is not distinct from auth.uid());
drop policy if exists interest_admin on public.interest_requests;
create policy interest_admin on public.interest_requests for all to authenticated using (public.is_admin()) with check (public.is_admin());
drop policy if exists feedback_submit on public.feedback;
create policy feedback_submit on public.feedback for insert to anon, authenticated with check (user_id is not distinct from auth.uid() and status='submitted');
drop policy if exists feedback_admin on public.feedback;
create policy feedback_admin on public.feedback for all to authenticated using (public.is_admin()) with check (public.is_admin());

-- No partners, people, events or communities are seeded by this migration.
commit;
