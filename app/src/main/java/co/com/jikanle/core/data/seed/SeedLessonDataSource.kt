package co.com.jikanle.core.data.seed

import android.content.Context
import co.com.jikanle.core.data.AppJson
import co.com.jikanle.core.domain.model.CulturalSlide
import co.com.jikanle.core.domain.model.DiscussionSlide
import co.com.jikanle.core.domain.model.GrammarNoteSlide
import co.com.jikanle.core.domain.model.IntroSlide
import co.com.jikanle.core.domain.model.Lesson
import co.com.jikanle.core.domain.model.LessonVisibility
import co.com.jikanle.core.domain.model.Level
import co.com.jikanle.core.domain.model.ListenSlide
import co.com.jikanle.core.domain.model.OutroSlide
import co.com.jikanle.core.domain.model.SecondListenSlide
import co.com.jikanle.core.domain.model.Slide
import co.com.jikanle.core.domain.model.SlideDeck
import co.com.jikanle.core.domain.model.Vocabulary
import co.com.jikanle.core.domain.model.VocabularySlide
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

const val FUYU_SEED_LESSON_ID = "seed-fuyu-no-hanashi"
private const val FUYU_SEED_SONG_ID = "seed-fuyu-no-hanashi-song"
private const val FUYU_SEED_ASSET = "fuyu_no_hanashi.json"

@Singleton
class SeedLessonDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun loadFuyuLesson(): Result<Lesson> = runCatching {
        val json = context.assets.open(FUYU_SEED_ASSET).bufferedReader().use { it.readText() }
        AppJson.decodeFromString(SeedLesson.serializer(), json).toDomain()
    }
}

@Serializable
private data class SeedLesson(
    val id: String,
    val song: SeedSong,
    val creator: SeedCreator,
    @SerialName("language_target") val languageTarget: String,
    @SerialName("language_explanation") val languageExplanation: String,
    val level: String,
    @SerialName("slide_deck") val slideDeck: SeedSlideDeck,
)

@Serializable
private data class SeedSong(
    @SerialName("title_original") val titleOriginal: String,
    @SerialName("title_romanized") val titleRomanized: String? = null,
    val artist: String,
    val language: String,
    val context: String,
    @SerialName("duration_ms") val durationMs: Long,
)

@Serializable
private data class SeedCreator(
    @SerialName("display_name") val displayName: String,
    val role: String,
)

@Serializable
private data class SeedSlideDeck(
    val slides: List<SeedSlide>,
)

@Serializable
private sealed interface SeedSlide {
    fun toDomain(index: Int, language: String): Slide
    fun vocabulary(language: String): List<Vocabulary> = emptyList()
}

@Serializable
@SerialName("intro")
private data class SeedIntroSlide(
    val title: String,
    val subtitle: String? = null,
    @SerialName("notes_es") val notesEs: String? = null,
    @SerialName("notes_en") val notesEn: String? = null,
) : SeedSlide {
    override fun toDomain(index: Int, language: String): Slide = IntroSlide(
        title = title,
        subtitle = subtitle,
        notes = localized(notesEs, notesEn, language),
    )
}

@Serializable
@SerialName("listen_first")
private data class SeedListenFirstSlide(
    @SerialName("instruction_es") val instructionEs: String? = null,
    @SerialName("instruction_en") val instructionEn: String? = null,
) : SeedSlide {
    override fun toDomain(index: Int, language: String): Slide = ListenSlide(
        songId = FUYU_SEED_SONG_ID,
        firstListenInstruction = localized(instructionEs, instructionEn, language),
    )
}

@Serializable
@SerialName("vocabulary")
private data class SeedVocabularySlide(
    val items: List<SeedVocabularyItem> = emptyList(),
) : SeedSlide {
    override fun toDomain(index: Int, language: String): Slide = VocabularySlide(
        items = items.mapIndexed { itemIndex, _ -> vocabularyId(itemIndex) },
    )

    override fun vocabulary(language: String): List<Vocabulary> = items.mapIndexed { itemIndex, item ->
        Vocabulary(
            id = vocabularyId(itemIndex),
            term = item.surface,
            reading = item.reading,
            meaning = localized(item.meaningEs, item.meaningEn, language).orEmpty(),
        )
    }
}

@Serializable
private data class SeedVocabularyItem(
    val surface: String,
    val reading: String? = null,
    @SerialName("meaning_es") val meaningEs: String,
    @SerialName("meaning_en") val meaningEn: String,
)

@Serializable
@SerialName("grammar")
private data class SeedGrammarSlide(
    val pattern: String,
    @SerialName("title_es") val titleEs: String? = null,
    @SerialName("title_en") val titleEn: String? = null,
    @SerialName("body_es") val bodyEs: String? = null,
    @SerialName("body_en") val bodyEn: String? = null,
) : SeedSlide {
    override fun toDomain(index: Int, language: String): Slide = GrammarNoteSlide(
        pattern = pattern,
        explanationMd = listOfNotNull(
            localized(titleEs, titleEn, language),
            localized(bodyEs, bodyEn, language),
        ).joinToString("\n\n").ifBlank { null },
    )
}

@Serializable
@SerialName("cultural")
private data class SeedCulturalSlide(
    val title: String,
    @SerialName("body_es") val bodyEs: String? = null,
    @SerialName("body_en") val bodyEn: String? = null,
) : SeedSlide {
    override fun toDomain(index: Int, language: String): Slide = CulturalSlide(
        title = title,
        bodyMd = localized(bodyEs, bodyEn, language),
    )
}

@Serializable
@SerialName("discussion")
private data class SeedDiscussionSlide(
    @SerialName("prompts_es") val promptsEs: List<String> = emptyList(),
    @SerialName("prompts_en") val promptsEn: List<String> = emptyList(),
) : SeedSlide {
    override fun toDomain(index: Int, language: String): Slide = DiscussionSlide(
        prompts = if (language == "en") promptsEn else promptsEs,
    )
}

@Serializable
@SerialName("listen_second")
private data class SeedListenSecondSlide(
    @SerialName("instruction_es") val instructionEs: String? = null,
    @SerialName("instruction_en") val instructionEn: String? = null,
) : SeedSlide {
    override fun toDomain(index: Int, language: String): Slide = SecondListenSlide(
        songId = FUYU_SEED_SONG_ID,
        withLyrics = true,
    )
}

@Serializable
@SerialName("outro")
private data class SeedOutroSlide(
    @SerialName("next_es") val nextEs: String? = null,
    @SerialName("next_en") val nextEn: String? = null,
) : SeedSlide {
    override fun toDomain(index: Int, language: String): Slide = OutroSlide(
        nextStepsMd = localized(nextEs, nextEn, language),
    )
}

private fun SeedLesson.toDomain(): Lesson {
    val slides = slideDeck.slides.mapIndexed { index, slide ->
        slide.toDomain(index, languageExplanation)
    }
    val vocabulary = slideDeck.slides.flatMap { it.vocabulary(languageExplanation) }
    return Lesson(
        id = id,
        songId = FUYU_SEED_SONG_ID,
        creatorId = null,
        title = song.titleRomanized ?: song.titleOriginal,
        description = "${song.artist} · ${song.titleOriginal} · ${song.context}",
        languageTarget = languageTarget,
        languageExplanation = languageExplanation,
        level = level.toLevel(),
        slideDeck = SlideDeck(slides),
        vocabularyPicks = vocabulary,
        discussionPrompts = slides.filterIsInstance<DiscussionSlide>().flatMap { it.prompts },
        culturalNotes = slides.filterIsInstance<CulturalSlide>().firstOrNull()?.bodyMd,
        visibility = LessonVisibility.PUBLIC,
        publishedAt = "2026-07-27T00:00:00Z",
    )
}

private fun localized(es: String?, en: String?, language: String): String? =
    if (language == "en") en ?: es else es ?: en

private fun vocabularyId(index: Int): String = "fuyu-vocab-${index + 1}"

private fun String.toLevel(): Level = when (lowercase()) {
    "beginner" -> Level.BEGINNER
    "advanced" -> Level.ADVANCED
    else -> Level.INTERMEDIATE
}
