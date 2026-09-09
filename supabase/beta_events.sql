-- Shared beta contract. Apply in staging first; this file does not deploy itself.
begin;
create table if not exists public.beta_events (
    id uuid primary key,
    user_id uuid not null references auth.users(id) on delete cascade,
    session_id uuid not null,
    event_name text not null check (event_name in (
        'app_opened', 'lesson_opened', 'lesson_started', 'lesson_completed',
        'vocabulary_opened', 'event_rsvp_opened', 'community_opened', 'feedback_submitted'
    )),
    app_version text not null check (length(app_version) between 1 and 64),
    lesson_id text check (length(lesson_id) between 1 and 200),
    language text not null check (length(language) between 1 and 35),
    occurred_at timestamptz not null,
    received_at timestamptz not null default now(),
    rating integer,
    check ((event_name = 'feedback_submitted' and rating is not null and rating between 1 and 5)
        or (event_name <> 'feedback_submitted' and rating is null))
);
create index if not exists beta_events_user_time_idx on public.beta_events (user_id, occurred_at);
alter table public.beta_events enable row level security;
revoke all on public.beta_events from anon, authenticated;
grant insert, select on public.beta_events to authenticated;
drop policy if exists beta_events_insert_own on public.beta_events;
create policy beta_events_insert_own on public.beta_events for insert to authenticated
    with check ((select auth.uid()) = user_id);
drop policy if exists beta_events_read_own on public.beta_events;
create policy beta_events_read_own on public.beta_events for select to authenticated
    using ((select auth.uid()) = user_id);
-- Clients cannot update/delete other observations. UUID retries use ON CONFLICT DO NOTHING.
commit;
