-- Jikanle translated-song MVP seed: Sakura Sakura (traditional/public domain).
-- Run after schema.sql. Safe to re-run.
--
-- PRIVATE COPYRIGHTED TESTS:
-- Fuyu no Hanashi and Yoru ni Kakeru may be processed locally only. Put source
-- lyrics in ml_models/songbridge/lyrics_private/ and generated JSON in its
-- ignored output/ directory. Import that data only into a private test project;
-- never copy those lyrics or generated translations into committed SQL/JSON.

begin;

insert into public.songs (
    id, title_original, title_romanized, artist, language, is_public_domain
) values (
    'a0000000-0000-4000-8000-000000000001',
    'さくら さくら',
    'Sakura Sakura',
    'Traditional',
    'ja',
    true
)
on conflict (id) do update set
    title_original = excluded.title_original,
    title_romanized = excluded.title_romanized,
    artist = excluded.artist,
    language = excluded.language,
    is_public_domain = excluded.is_public_domain;

insert into public.song_lyric_lines (
    song_id, line_index, language, text, transliteration
) values
    ('a0000000-0000-4000-8000-000000000001', 0, 'ja', 'さくら さくら', 'sakura sakura'),
    ('a0000000-0000-4000-8000-000000000001', 1, 'ja', '野山も里も', 'noyama mo sato mo'),
    ('a0000000-0000-4000-8000-000000000001', 2, 'ja', '見渡す限り', 'miwatasu kagiri'),
    ('a0000000-0000-4000-8000-000000000001', 3, 'ja', '霞か雲か', 'kasumi ka kumo ka'),
    ('a0000000-0000-4000-8000-000000000001', 4, 'ja', '朝日に匂う', 'asahi ni niou'),
    ('a0000000-0000-4000-8000-000000000001', 5, 'ja', 'さくら さくら', 'sakura sakura'),
    ('a0000000-0000-4000-8000-000000000001', 6, 'ja', '花ざかり', 'hanazakari')
on conflict (song_id, language, line_index) do update set
    text = excluded.text,
    transliteration = excluded.transliteration;

insert into public.song_translations (
    id, song_id, source_language, target_language, provider, alignment_report
) values
    (
        'a0000000-0000-4000-8000-000000000101',
        'a0000000-0000-4000-8000-000000000001',
        'ja', 'es', 'jikanle-manual-v1',
        'Seed review: all seven lines are within one target unit of the source.'
    ),
    (
        'a0000000-0000-4000-8000-000000000102',
        'a0000000-0000-4000-8000-000000000001',
        'ja', 'en', 'jikanle-manual-v1',
        'Seed review: all seven lines are within one target unit of the source.'
    )
on conflict (song_id, source_language, target_language) do update set
    provider = excluded.provider,
    alignment_report = excluded.alignment_report;

insert into public.song_translation_lines (
    translation_id, line_index, translated_text, source_units, target_units,
    emotion, stressed, singability_note
) values
    ('a0000000-0000-4000-8000-000000000101', 0, 'Cerezos, cerezos', 6, 6, 'wonder', 'cerezos', 'Mirrors the repeated opening.'),
    ('a0000000-0000-4000-8000-000000000101', 1, 'Por campos y aldeas', 7, 7, 'expansive', 'campos, aldeas', 'Keeps the landscape sweep.'),
    ('a0000000-0000-4000-8000-000000000101', 2, 'Hasta el horizonte', 7, 7, 'awe', 'horizonte', 'Compresses the view into one image.'),
    ('a0000000-0000-4000-8000-000000000101', 3, '¿Es niebla o son nubes?', 7, 7, 'wonder', 'niebla, nubes', 'Preserves the central question.'),
    ('a0000000-0000-4000-8000-000000000101', 4, 'Perfuman la luz del alba', 7, 8, 'radiant', 'luz, alba', 'One extra unit preserves the morning image.'),
    ('a0000000-0000-4000-8000-000000000101', 5, 'Cerezos, cerezos', 6, 6, 'wonder', 'cerezos', 'Repeats the opening phrase.'),
    ('a0000000-0000-4000-8000-000000000101', 6, 'Flores en plenitud', 5, 6, 'joy', 'plenitud', 'Ends on the full-bloom image.'),
    ('a0000000-0000-4000-8000-000000000102', 0, 'Cherry blooms, cherry blooms', 6, 6, 'wonder', 'cherry blooms', 'Mirrors the repeated opening.'),
    ('a0000000-0000-4000-8000-000000000102', 1, 'Through fields and villages', 7, 6, 'expansive', 'fields, villages', 'Keeps the landscape sweep.'),
    ('a0000000-0000-4000-8000-000000000102', 2, 'As far as the eye can see', 7, 7, 'awe', 'eye, see', 'Preserves the breadth of the view.'),
    ('a0000000-0000-4000-8000-000000000102', 3, 'Is that mist or clouds above?', 7, 7, 'wonder', 'mist, clouds', 'Preserves the central question.'),
    ('a0000000-0000-4000-8000-000000000102', 4, 'Fragrant in the morning light', 7, 7, 'radiant', 'morning light', 'Keeps fragrance and dawn together.'),
    ('a0000000-0000-4000-8000-000000000102', 5, 'Cherry blooms, cherry blooms', 6, 6, 'wonder', 'cherry blooms', 'Repeats the opening phrase.'),
    ('a0000000-0000-4000-8000-000000000102', 6, 'Flowers in full bloom', 5, 5, 'joy', 'full bloom', 'Ends on the full-bloom image.')
on conflict (translation_id, line_index) do update set
    translated_text = excluded.translated_text,
    source_units = excluded.source_units,
    target_units = excluded.target_units,
    emotion = excluded.emotion,
    stressed = excluded.stressed,
    singability_note = excluded.singability_note;

insert into public.song_vocabulary (
    song_id, line_index, language, term, reading, meaning, explanation
) values
    ('a0000000-0000-4000-8000-000000000001', 0, 'ja', 'さくら', 'sakura', 'cherry blossom', 'A symbol of spring and impermanence in Japan.'),
    ('a0000000-0000-4000-8000-000000000001', 1, 'ja', '野山', 'noyama', 'fields and hills', 'A broad natural landscape outside the city.'),
    ('a0000000-0000-4000-8000-000000000001', 3, 'ja', '霞', 'kasumi', 'spring haze', 'A fine seasonal mist often used in poetry.'),
    ('a0000000-0000-4000-8000-000000000001', 6, 'ja', '花ざかり', 'hanazakari', 'full bloom', 'The moment when flowers are at their peak.')
on conflict (song_id, language, term) do update set
    line_index = excluded.line_index,
    reading = excluded.reading,
    meaning = excluded.meaning,
    explanation = excluded.explanation;

commit;
