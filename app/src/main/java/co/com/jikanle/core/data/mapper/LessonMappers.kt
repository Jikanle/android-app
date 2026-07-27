package co.com.jikanle.core.data.mapper

import co.com.jikanle.core.data.local.entity.LessonEntity
import co.com.jikanle.core.domain.model.Lesson

fun LessonEntity.toDomain(): Lesson = Lesson(
    id = id,
    songId = songId,
    creatorId = creatorId,
    title = title,
    description = description,
    languageTarget = languageTarget,
    languageExplanation = languageExplanation,
    level = level,
    slideDeck = slideDeck,
    vocabularyPicks = vocabularyPicks,
    discussionPrompts = discussionPrompts,
    culturalNotes = culturalNotes,
    isPaid = isPaid,
    priceCop = priceCop,
    visibility = visibility,
    publishedAt = publishedAt,
)

fun Lesson.toEntity(): LessonEntity = LessonEntity(
    id = id,
    songId = songId,
    creatorId = creatorId,
    title = title,
    description = description,
    languageTarget = languageTarget,
    languageExplanation = languageExplanation,
    level = level,
    slideDeck = slideDeck,
    vocabularyPicks = vocabularyPicks,
    discussionPrompts = discussionPrompts,
    culturalNotes = culturalNotes,
    isPaid = isPaid,
    priceCop = priceCop,
    visibility = visibility,
    publishedAt = publishedAt,
)
