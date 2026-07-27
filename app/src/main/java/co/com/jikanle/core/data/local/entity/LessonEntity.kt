package co.com.jikanle.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.com.jikanle.core.domain.model.Level
import co.com.jikanle.core.domain.model.LessonVisibility
import co.com.jikanle.core.domain.model.SlideDeck
import co.com.jikanle.core.domain.model.Vocabulary

/** Offline cache row for [co.com.jikanle.core.domain.model.Lesson]. */
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val songId: String,
    val creatorId: String?,
    val title: String,
    val description: String?,
    val languageTarget: String,
    val languageExplanation: String,
    val level: Level,
    val slideDeck: SlideDeck,
    val vocabularyPicks: List<Vocabulary>,
    val discussionPrompts: List<String>,
    val culturalNotes: String?,
    val isPaid: Boolean,
    val priceCop: Int?,
    val visibility: LessonVisibility,
    val publishedAt: String?,
)
