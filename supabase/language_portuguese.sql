-- Apply after schema.sql, in staging first. No content, permissions or RLS changes.
begin;
alter table public.song_lyric_lines drop constraint if exists song_lyric_lines_language_check;
alter table public.song_lyric_lines add constraint song_lyric_lines_language_check
    check (language in ('ja','en','es','zh','pt'));
alter table public.song_translations drop constraint if exists song_translations_source_language_check;
alter table public.song_translations add constraint song_translations_source_language_check
    check (source_language in ('ja','en','es','zh','pt'));
alter table public.song_translations drop constraint if exists song_translations_target_language_check;
alter table public.song_translations add constraint song_translations_target_language_check
    check (target_language in ('ja','en','es','zh','pt'));
alter table public.song_vocabulary drop constraint if exists song_vocabulary_language_check;
alter table public.song_vocabulary add constraint song_vocabulary_language_check
    check (language in ('ja','en','es','zh','pt'));
commit;
