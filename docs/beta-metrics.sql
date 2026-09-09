-- Operator-only queries in Supabase SQL Editor. Never expose an admin key to a client.
-- These describe consenting signed-in testers, not all installs or learning gains.
select date_trunc('day', occurred_at at time zone 'America/Bogota') as day,
       event_name, count(*) as events, count(distinct user_id) as testers
from public.beta_events
group by 1, 2 order by 1 desc, 2;

-- Completion among observed lesson starters. Multiple reviews count once per user/lesson.
with starters as (
  select distinct user_id, lesson_id from public.beta_events where event_name = 'lesson_started'
), finishers as (
  select distinct user_id, lesson_id from public.beta_events where event_name = 'lesson_completed'
)
select s.lesson_id, count(*) as starters, count(f.user_id) as completers
from starters s left join finishers f using (user_id, lesson_id) group by 1;

-- D1/D7 exact calendar-day return; exclude cohorts that have not reached the observation day.
with days as (
  select distinct user_id, (occurred_at at time zone 'America/Bogota')::date as day
  from public.beta_events where event_name = 'app_opened'
), cohorts as (
  select user_id, min(day) as first_day from days group by 1
)
select offset_days, count(*) as eligible_testers,
       count(d.user_id) as returned_testers
from cohorts c cross join (values (1), (7)) offsets(offset_days)
left join days d on d.user_id = c.user_id and d.day = c.first_day + offset_days
where c.first_day + offset_days < (now() at time zone 'America/Bogota')::date
group by 1;
