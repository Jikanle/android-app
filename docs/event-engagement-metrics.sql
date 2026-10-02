-- Operator-only. Requires reviewed event_engagement.sql. No public attendee views.
-- A click is not an RSVP. An RSVP is not a host-verified attendance.
-- Consent at collection AND current opt-in are required. Revocation excludes history.
-- Week boundaries are Monday 00:00 America/Bogota, not the device timezone.
with attendance as (
    select p.user_id, p.event_id, e.language, e.starts_at,
           date_trunc('week', e.starts_at at time zone 'America/Bogota') as week
    from public.event_participations p
    join public.events e on e.id = p.event_id
    join public.event_preferences pref on pref.user_id = p.user_id
    where p.checked_in_at is not null and p.analytics_allowed and pref.metrics_opt_in
      and e.starts_at < now() and e.language is not null
), counts as (
    select week, language, user_id, count(*) as visits
    from attendance group by 1, 2, 3
)
select week, language, count(*) as attendees,
       count(*) filter (where visits >= 2) as attended_two_or_more,
       round(100.0 * count(*) filter (where visits >= 2) / nullif(count(*), 0), 1) as repeat_pct
from counts where week < date_trunc('week', now() at time zone 'America/Bogota')
group by 1, 2 having count(*) >= 5 order by 1 desc, 2;

-- Next verified attendance, across ALL languages, within seven elapsed days.
-- Only first observed attendance per person is an index visit; require a full window.
-- Suppression <5 is a display safeguard, not a guarantee of anonymization.
with attendance as (
    select p.user_id, p.event_id, e.language, e.starts_at
    from public.event_participations p
    join public.events e on e.id = p.event_id
    join public.event_preferences pref on pref.user_id = p.user_id
    where p.checked_in_at is not null and p.analytics_allowed and pref.metrics_opt_in
      and e.starts_at < now()
), sequence as (
    select *, row_number() over w as visit,
           lead(language) over w as next_language, lead(starts_at) over w as next_start
    from attendance window w as (partition by user_id order by starts_at, event_id)
)
select language, count(*) as eligible_first_attendees,
       count(*) filter (where next_language = language and next_start > starts_at
          and next_start <= starts_at + interval '7 days') as returned_same_language_next
from sequence where visit = 1 and language is not null and starts_at <= now() - interval '7 days'
group by language having count(*) >= 5;
