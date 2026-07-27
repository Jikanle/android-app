package co.com.jikanle.core.data

import co.com.jikanle.core.domain.model.CulturalSlide
import co.com.jikanle.core.domain.model.DiscussionSlide
import co.com.jikanle.core.domain.model.GrammarNoteSlide
import co.com.jikanle.core.domain.model.IntroSlide
import co.com.jikanle.core.domain.model.ListenSlide
import co.com.jikanle.core.domain.model.LyricFocusSlide
import co.com.jikanle.core.domain.model.OutroSlide
import co.com.jikanle.core.domain.model.SecondListenSlide
import co.com.jikanle.core.domain.model.SlideDeck
import co.com.jikanle.core.domain.model.VocabularySlide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Proves the polymorphic [SlideDeck] decodes from the exact JSON shape stored in
 * `supabase/seed_fuyu_no_hanashi.sql` (slide_deck jsonb). This is the riskiest part of the
 * data layer — the discriminator "type" mapping each slide to its data class — so verify it
 * at runtime, not just by compiling.
 */
class SlideDeckSerializationTest {

    private val fuyuNoHanashiDeck = """
    {
      "slides": [
        { "type": "intro", "title": "冬のはなし", "subtitle": "Centimillimental",
          "cover_url": "https://x/cover.jpg", "notes": "host notes" },
        { "type": "listen", "song_id": "11111111-1111-1111-1111-111111111111",
          "first_listen_instruction": "Solo escucha." },
        { "type": "vocabulary", "items": ["a0000000-0000-0000-0000-000000000001"] },
        { "type": "grammar_note", "pattern": "AのB",
          "explanation_md": "...", "examples": ["冬のはなし"] },
        { "type": "lyric_focus", "lyric_range": { "start_ms": 45000, "end_ms": 72000 },
          "explanation_md": "..." },
        { "type": "cultural", "title": "物の哀れ", "body_md": "..." },
        { "type": "discussion", "prompts": ["¿A qué te suena el invierno?"] },
        { "type": "second_listen", "song_id": "11111111-1111-1111-1111-111111111111",
          "with_lyrics": true },
        { "type": "outro", "next_steps_md": "...", "linked_lessons": [] }
      ]
    }
    """.trimIndent()

    @Test
    fun `deck decodes every slide type to its data class`() {
        val deck = AppJson.decodeFromString(SlideDeck.serializer(), fuyuNoHanashiDeck)

        assertEquals(9, deck.slides.size)
        assertTrue(deck.slides[0] is IntroSlide)
        assertTrue(deck.slides[1] is ListenSlide)
        assertTrue(deck.slides[2] is VocabularySlide)
        assertTrue(deck.slides[3] is GrammarNoteSlide)
        assertTrue(deck.slides[4] is LyricFocusSlide)
        assertTrue(deck.slides[5] is CulturalSlide)
        assertTrue(deck.slides[6] is DiscussionSlide)
        assertTrue(deck.slides[7] is SecondListenSlide)
        assertTrue(deck.slides[8] is OutroSlide)

        // Spot-check @SerialName-mapped fields survive the round trip.
        assertEquals("冬のはなし", (deck.slides[0] as IntroSlide).title)
        assertEquals(45000L, (deck.slides[4] as LyricFocusSlide).lyricRange?.startMs)
        assertTrue((deck.slides[7] as SecondListenSlide).withLyrics)
    }

    @Test
    fun `deck round-trips through encode and decode`() {
        val deck = AppJson.decodeFromString(SlideDeck.serializer(), fuyuNoHanashiDeck)
        val reencoded = AppJson.encodeToString(SlideDeck.serializer(), deck)
        val again = AppJson.decodeFromString(SlideDeck.serializer(), reencoded)
        assertEquals(deck, again)
    }
}
