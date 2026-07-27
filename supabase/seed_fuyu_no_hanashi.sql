-- ============================================================================
-- Jikanle — Seed: "冬のはなし / Fuyu no Hanashi" (Centimillimental, from «Given»)
-- The canción de la noche for Event #0 at Casa Alternativa (Bogotá).
--
-- Run AFTER supabase/schema.sql, in the Supabase SQL editor. Idempotent
-- (on conflict do nothing) — safe to re-run.
--
-- ⚠️ THIS SQL HAS NOT BEEN RUN against Postgres — it's drafted, not verified. Paste any
--    error from the Supabase SQL editor back and I'll fix it.
--
-- ⚠️ CONTENT IS A DRAFT — VERIFY BEFORE TEACHING (Alejandro, you're the teacher):
--   1. The 8 vocabulary items are THEMATIC vocab chosen for a winter/loss session — they are
--      NOT extracted from the actual lyrics of 冬のはなし (I don't have the lyrics). Confirm
--      which words truly appear in the song and swap as needed. reading/meaning are drafted.
--   2. lyric_focus.lyric_range (45–72s) and its text are PLACEHOLDERS. The deck does NOT know
--      which words appear where — pick a real line and align timestamps to the real LRC.
--   3. AUDIO + LRC are PLACEHOLDERS. Audio licensing is an open blocker (Build Brief §14 Q2) —
--      likely link to Spotify/YouTube for full plays and self-host only a ~30s clip.
--   4. release_year = 2019 is a GUESS — verify.
--   5. Explanations are in Spanish (Bogotá audience). To teach in English, set
--      lessons.language_explanation = 'en' and translate the deck text.
--   6. event starts_at/ends_at default to Sat 2026-06-27 17:00–20:00 (America/Bogotá). Change it.
--   7. host_id / creator_id are NULL until you sign in — then set them to your uid:
--        update public.events  set host_id    = '<your-uid>' where id = '33333333-3333-3333-3333-333333333333';
--        update public.lessons set creator_id = '<your-uid>' where id = '22222222-2222-2222-2222-222222222222';
-- ============================================================================

-- ---- Song ------------------------------------------------------------------
insert into public.songs (
    id, title_original, title_romanized, artist, language,
    audio_url, cover_url, duration_ms, release_year, lrc_url, vocabulary_seed_ids
) values (
    '11111111-1111-1111-1111-111111111111',
    '冬のはなし',
    'Fuyu no Hanashi',
    'Centimillimental',
    'ja',
    'https://PLACEHOLDER/songs/fuyu-no-hanashi-clip.mp3',   -- TODO: licensed clip
    'https://PLACEHOLDER/lesson-covers/fuyu-no-hanashi.jpg', -- TODO: cover art
    null,        -- duration_ms unknown; fill from the real file
    2019,
    null,        -- lrc_url: upload aligned LRC, then set
    array[
        'a0000000-0000-0000-0000-000000000001',
        'a0000000-0000-0000-0000-000000000002',
        'a0000000-0000-0000-0000-000000000003',
        'a0000000-0000-0000-0000-000000000004',
        'a0000000-0000-0000-0000-000000000005',
        'a0000000-0000-0000-0000-000000000006',
        'a0000000-0000-0000-0000-000000000007',
        'a0000000-0000-0000-0000-000000000008'
    ]::uuid[]
)
on conflict (id) do nothing;

-- ---- Vocabulary (8 picks, Build Brief §12) ---------------------------------
insert into public.vocabulary (id, term, reading, meaning, example) values
    ('a0000000-0000-0000-0000-000000000001', '冬',     'ふゆ',     'invierno',                       'もうすぐ冬だね。(Ya casi es invierno, ¿no?)'),
    ('a0000000-0000-0000-0000-000000000002', '季節',   'きせつ',   'estación (del año)',             '季節が変わると、君を思い出す。(Cuando cambian las estaciones, me acuerdo de ti.)'),
    ('a0000000-0000-0000-0000-000000000003', '君',     'きみ',     'tú (forma íntima y afectuosa)',  '君がいた冬。(El invierno en que estabas tú.)'),
    ('a0000000-0000-0000-0000-000000000004', '思い出', 'おもいで', 'recuerdo(s)',                    'これは大切な思い出だ。(Este es un recuerdo importante.)'),
    ('a0000000-0000-0000-0000-000000000005', '笑顔',   'えがお',   'sonrisa',                        '君の笑顔を覚えている。(Recuerdo tu sonrisa.)'),
    ('a0000000-0000-0000-0000-000000000006', '涙',     'なみだ',   'lágrimas',                       '涙がこぼれた。(Se me cayeron las lágrimas.)'),
    ('a0000000-0000-0000-0000-000000000007', '言葉',   'ことば',   'palabra(s); idioma',             '言葉にできない。(No puedo expresarlo en palabras.)'),
    ('a0000000-0000-0000-0000-000000000008', '終わり', 'おわり',   'el final',                       'これで終わりじゃない。(Esto no es el final.)')
on conflict (id) do nothing;

-- ---- Lesson #1 (slide deck per Build Brief §7.2) ---------------------------
insert into public.lessons (
    id, song_id, creator_id, title, description,
    language_target, language_explanation, level,
    slide_deck, vocabulary_picks, discussion_prompts, cultural_notes,
    is_paid, price_cop, visibility, published_at
) values (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    null,  -- creator_id: set to your uid after signup
    '冬のはなし — Una historia de invierno',
    'Aprende japonés a través de «冬のはなし» de Centimillimental (de la serie «Given»). Vocabulario esencial, una nota de gramática y la estética del 物の哀れ. La canción de la noche del primer encuentro Jikanle.',
    'ja',
    'es',
    'beginner',
    $deck$
    {
      "slides": [
        { "type": "intro",
          "title": "冬のはなし",
          "subtitle": "Centimillimental · de la serie «Given»",
          "cover_url": "https://PLACEHOLDER/lesson-covers/fuyu-no-hanashi.jpg",
          "notes": "Marco para el anfitrión: presenta la canción sin spoilear la letra. Pregunta quién conoce «Given». Conecta el invierno con la idea de las cosas que terminan." },
        { "type": "listen",
          "song_id": "11111111-1111-1111-1111-111111111111",
          "first_listen_instruction": "Solo escucha. Sin letra ni traducción. ¿Qué emoción te deja?" },
        { "type": "vocabulary",
          "items": [
            "a0000000-0000-0000-0000-000000000001",
            "a0000000-0000-0000-0000-000000000002",
            "a0000000-0000-0000-0000-000000000003",
            "a0000000-0000-0000-0000-000000000004",
            "a0000000-0000-0000-0000-000000000005",
            "a0000000-0000-0000-0000-000000000006",
            "a0000000-0000-0000-0000-000000000007",
            "a0000000-0000-0000-0000-000000000008"
          ] },
        { "type": "grammar_note",
          "pattern": "AのB （partícula の）",
          "explanation_md": "La partícula **の** une dos sustantivos: **A の B** = «B de A». El título 冬**の**はなし significa «la historia **del** invierno». No siempre se traduce como «de»: también marca posesión o tipo.",
          "examples": ["冬のはなし — la historia del invierno", "私の名前 — mi nombre", "日本語の歌 — una canción en japonés"] },
        { "type": "lyric_focus",
          "lyric_range": { "start_ms": 45000, "end_ms": 72000 },
          "explanation_md": "(Placeholder — rango y línea por definir.) Cuando subas el LRC real, elige una línea del estribillo y alinea estos tiempos. Trabaja aquí una estructura gramatical que de verdad aparezca en esa línea." },
        { "type": "cultural",
          "title": "物の哀れ (mono no aware)",
          "body_md": "**物の哀れ** es la sensibilidad japonesa hacia lo efímero: la belleza melancólica de las cosas *porque* se acaban. El invierno, la nieve que se derrite, una persona que se va — la canción vive en ese sentimiento. No es tristeza pura: es ternura por lo que pasó." },
        { "type": "discussion",
          "prompts": ["¿A qué te suena el invierno en tu idioma? ¿Qué palabras o imágenes evoca?"] },
        { "type": "discussion",
          "prompts": ["La canción habla de alguien que ya no está. ¿Hay una canción que te devuelva a una persona o a un momento?"] },
        { "type": "second_listen",
          "song_id": "11111111-1111-1111-1111-111111111111",
          "with_lyrics": true },
        { "type": "outro",
          "next_steps_md": "Esta es la Lección #1 de la Biblioteca Jikanle. Sigue practicando en el Open Room y busca a alguien con quien continuar la conversación. 続き — la historia sigue.",
          "linked_lessons": [] }
      ]
    }
    $deck$::jsonb,
    $picks$
    [
      { "id": "a0000000-0000-0000-0000-000000000001", "term": "冬",     "reading": "ふゆ",     "meaning": "invierno",                      "example": "もうすぐ冬だね。(Ya casi es invierno, ¿no?)" },
      { "id": "a0000000-0000-0000-0000-000000000002", "term": "季節",   "reading": "きせつ",   "meaning": "estación (del año)",            "example": "季節が変わると、君を思い出す。(Cuando cambian las estaciones, me acuerdo de ti.)" },
      { "id": "a0000000-0000-0000-0000-000000000003", "term": "君",     "reading": "きみ",     "meaning": "tú (forma íntima y afectuosa)", "example": "君がいた冬。(El invierno en que estabas tú.)" },
      { "id": "a0000000-0000-0000-0000-000000000004", "term": "思い出", "reading": "おもいで", "meaning": "recuerdo(s)",                   "example": "これは大切な思い出だ。(Este es un recuerdo importante.)" },
      { "id": "a0000000-0000-0000-0000-000000000005", "term": "笑顔",   "reading": "えがお",   "meaning": "sonrisa",                       "example": "君の笑顔を覚えている。(Recuerdo tu sonrisa.)" },
      { "id": "a0000000-0000-0000-0000-000000000006", "term": "涙",     "reading": "なみだ",   "meaning": "lágrimas",                      "example": "涙がこぼれた。(Se me cayeron las lágrimas.)" },
      { "id": "a0000000-0000-0000-0000-000000000007", "term": "言葉",   "reading": "ことば",   "meaning": "palabra(s); idioma",            "example": "言葉にできない。(No puedo expresarlo en palabras.)" },
      { "id": "a0000000-0000-0000-0000-000000000008", "term": "終わり", "reading": "おわり",   "meaning": "el final",                      "example": "これで終わりじゃない。(Esto no es el final.)" }
    ]
    $picks$::jsonb,
    array[
        '¿A qué te suena el invierno en tu idioma? ¿Qué palabras o imágenes evoca?',
        'La canción habla de alguien que ya no está. ¿Hay una canción que te devuelva a una persona o a un momento?'
    ]::text[],
    '物の哀れ (mono no aware): la belleza melancólica de lo efímero. El hilo emocional de toda la lección.',
    false,
    null,
    'public',
    now()
)
on conflict (id) do nothing;

-- ---- Event #0 + its Room (Casa Alternativa, Bogotá) ------------------------
-- Insert the event first (room_id null), then the room, then close the 1:1 link.
insert into public.events (
    id, title, description, host_id,
    starts_at, ends_at, venue_name, venue_location, luma_url, cover_image_url,
    featured_song_id, playlist_song_ids, room_id, ticket_price_cop, max_attendees
) values (
    '33333333-3333-3333-3333-333333333333',
    'Jikanle #0 — 冬のはなし en Casa Alternativa',
    'El primer encuentro Jikanle. Una noche en Casa Alternativa (Bogotá) para aprender japonés a través de la música: escuchamos «冬のはなし», desarmamos su letra y nos quedamos a conversar. Entrada libre. 時間 / 出会い / 乐 — tiempo, encuentro, música.',
    null,  -- host_id: set to your uid after signup
    '2026-06-27 17:00:00-05',
    '2026-06-27 20:00:00-05',
    'Casa Alternativa',
    'Bogotá, Colombia',
    'https://luma.com/Jikanle?k=c',
    'https://PLACEHOLDER/lesson-covers/fuyu-no-hanashi.jpg',
    '11111111-1111-1111-1111-111111111111',
    array['11111111-1111-1111-1111-111111111111']::uuid[],
    null,   -- room_id set below
    null,   -- ticket_price_cop null => free
    20
)
on conflict (id) do nothing;

insert into public.rooms (id, event_id, name, description) values (
    '44444444-4444-4444-4444-444444444444',
    '33333333-3333-3333-3333-333333333333',
    '冬のはなし · Casa Alternativa',
    'La sala de continuidad para quienes vinieron al primer encuentro. 続き — sigamos la conversación que empezó esa noche.'
)
on conflict (id) do nothing;

update public.events
    set room_id = '44444444-4444-4444-4444-444444444444'
    where id = '33333333-3333-3333-3333-333333333333';
