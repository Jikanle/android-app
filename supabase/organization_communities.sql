-- ============================================================================
-- Organization community links — Android beta companion to the public calendar.
-- Run after organizations_and_calendar.sql in the Supabase SQL editor.
--
-- Store only an organization-controlled public invitation or landing page.
-- Do not put member lists, access tokens, or private group URLs in this column.
-- ============================================================================

alter table public.organizations
    add column if not exists community_url text;

comment on column public.organizations.community_url is
    'Public community landing or invitation URL shown with that organization’s events.';
