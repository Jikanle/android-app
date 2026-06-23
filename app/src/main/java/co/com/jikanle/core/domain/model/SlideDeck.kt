package co.com.jikanle.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Structured slide content for a Lesson (CLAUDE.md §7.2). Serialized as the
 * `slide_deck` jsonb column. kotlinx.serialization's default class discriminator is
 * "type", which matches the schema's `"type"` field, so each [Slide] subtype maps 1:1
 * to a JSON object and to a Composable in the reader.
 */
@Serializable
data class SlideDeck(
    val slides: List<Slide> = emptyList(),
)

@Serializable
data class LyricRange(
    @SerialName("start_ms") val startMs: Long,
    @SerialName("end_ms") val endMs: Long,
)

@Serializable
sealed interface Slide

@Serializable
@SerialName("intro")
data class IntroSlide(
    val title: String,
    val subtitle: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    val notes: String? = null,
) : Slide

@Serializable
@SerialName("listen")
data class ListenSlide(
    @SerialName("song_id") val songId: String,
    @SerialName("first_listen_instruction") val firstListenInstruction: String? = null,
) : Slide

@Serializable
@SerialName("vocabulary")
data class VocabularySlide(
    /** Vocabulary ids, resolved against the Lesson's vocabulary_picks at render time. */
    val items: List<String> = emptyList(),
) : Slide

@Serializable
@SerialName("lyric_focus")
data class LyricFocusSlide(
    @SerialName("lyric_range") val lyricRange: LyricRange? = null,
    @SerialName("explanation_md") val explanationMd: String? = null,
) : Slide

@Serializable
@SerialName("grammar_note")
data class GrammarNoteSlide(
    val pattern: String,
    @SerialName("explanation_md") val explanationMd: String? = null,
    val examples: List<String> = emptyList(),
) : Slide

@Serializable
@SerialName("cultural")
data class CulturalSlide(
    val title: String,
    @SerialName("body_md") val bodyMd: String? = null,
) : Slide

@Serializable
@SerialName("discussion")
data class DiscussionSlide(
    val prompts: List<String> = emptyList(),
) : Slide

@Serializable
@SerialName("second_listen")
data class SecondListenSlide(
    @SerialName("song_id") val songId: String,
    @SerialName("with_lyrics") val withLyrics: Boolean = true,
) : Slide

@Serializable
@SerialName("outro")
data class OutroSlide(
    @SerialName("next_steps_md") val nextStepsMd: String? = null,
    @SerialName("linked_lessons") val linkedLessons: List<String> = emptyList(),
) : Slide
