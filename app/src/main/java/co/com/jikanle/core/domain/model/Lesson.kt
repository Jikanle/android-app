package co.com.jikanle.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Creator's perspective on a Song (CLAUDE.md §6/§7). Multiple Lessons can attach to
 * the same Song. The [slideDeck] drives the reader; [vocabularyPicks] resolve the ids
 * referenced by any [VocabularySlide].
 */
@Serializable
data class Lesson(
    val id: String,
    @SerialName("song_id") val songId: String,
    @SerialName("creator_id") val creatorId: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("language_target") val languageTarget: String,
    @SerialName("language_explanation") val languageExplanation: String,
    val level: Level,
    @SerialName("slide_deck") val slideDeck: SlideDeck = SlideDeck(),
    @SerialName("vocabulary_picks") val vocabularyPicks: List<Vocabulary> = emptyList(),
    @SerialName("discussion_prompts") val discussionPrompts: List<String> = emptyList(),
    @SerialName("cultural_notes") val culturalNotes: String? = null,
    @SerialName("is_paid") val isPaid: Boolean = false,
    @SerialName("price_cop") val priceCop: Int? = null,
    val visibility: LessonVisibility = LessonVisibility.PUBLIC,
    @SerialName("published_at") val publishedAt: String? = null,
)
