-- ============================================================================
-- Invite-only host onboarding (curated leaders of each sub-learning group).
-- Additive + idempotent. Run in the Supabase SQL editor after
-- organizations_and_calendar.sql.
--
-- Flow: an admin invites an EMAIL to an organization (before that person even
-- has an account). When they sign up — or next time they open /host — the
-- invite is redeemed automatically into an organization_members row.
-- ============================================================================

create table if not exists public.organization_invites (
    id              uuid        primary key default gen_random_uuid(),
    organization_id uuid        not null references public.organizations (id) on delete cascade,
    email           text        not null,
    role            text        not null default 'editor' check (role in ('owner','editor')),
    invited_by      uuid        references public.profiles (id) on delete set null,
    redeemed_at     timestamptz,
    redeemed_by     uuid        references public.profiles (id) on delete set null,
    created_at      timestamptz not null default now(),
    unique (organization_id, email)
);
create index if not exists organization_invites_email_idx
    on public.organization_invites (lower(email)) where redeemed_at is null;

alter table public.organization_invites enable row level security;

-- Admins manage invites; an invitee may see their own pending invite.
drop policy if exists org_invites_admin_all on public.organization_invites;
create policy org_invites_admin_all on public.organization_invites
    for all to authenticated using (public.is_admin()) with check (public.is_admin());

drop policy if exists org_invites_see_own on public.organization_invites;
create policy org_invites_see_own on public.organization_invites
    for select to authenticated
    using (lower(email) = lower(coalesce(auth.jwt() ->> 'email', '')));

-- ---- redemption -------------------------------------------------------------
-- Turns every pending invite matching the caller's email into a membership.
-- SECURITY DEFINER so it can write organization_members (whose writes are admin-only).
create or replace function public.redeem_my_org_invites()
returns int language plpgsql security definer set search_path = public as $$
declare
    v_email text := lower(coalesce(auth.jwt() ->> 'email', ''));
    v_uid   uuid := auth.uid();
    v_count int  := 0;
begin
    if v_uid is null or v_email = '' then
        return 0;
    end if;

    insert into public.organization_members (organization_id, profile_id, role)
    select i.organization_id, v_uid, i.role
    from public.organization_invites i
    where lower(i.email) = v_email and i.redeemed_at is null
    on conflict (organization_id, profile_id) do update set role = excluded.role;

    update public.organization_invites
       set redeemed_at = now(), redeemed_by = v_uid
     where lower(email) = v_email and redeemed_at is null;
    get diagnostics v_count = row_count;

    -- Being a host implies the 'host' role on the profile.
    if v_count > 0 then
        update public.profiles
           set roles = (select array(select distinct unnest(roles || array['host'])))
         where id = v_uid;
    end if;

    return v_count;
end;
$$;

grant execute on function public.redeem_my_org_invites() to authenticated;

-- Redeem automatically at signup, too (so invited people are hosts on first login).
create or replace function public.handle_new_user_invites()
returns trigger language plpgsql security definer set search_path = public as $$
begin
    insert into public.organization_members (organization_id, profile_id, role)
    select i.organization_id, new.id, i.role
    from public.organization_invites i
    where lower(i.email) = lower(coalesce(new.email, '')) and i.redeemed_at is null
    on conflict (organization_id, profile_id) do nothing;

    update public.organization_invites
       set redeemed_at = now(), redeemed_by = new.id
     where lower(email) = lower(coalesce(new.email, '')) and redeemed_at is null;

    return new;
end;
$$;

drop trigger if exists on_auth_user_created_invites on auth.users;
create trigger on_auth_user_created_invites
    after insert on auth.users
    for each row execute function public.handle_new_user_invites();

-- ---- bootstrap: make the founder an admin ----------------------------------
-- Run ONCE, replacing the email, to grant yourself the admin role.
--   update public.profiles set roles = (select array(select distinct unnest(roles || array['admin','host'])))
--    where id = (select id from auth.users where lower(email) = lower('alesanchezpov@gmail.com'));
