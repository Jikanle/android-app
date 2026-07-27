package co.com.jikanle.core.data.mapper

import co.com.jikanle.core.data.local.entity.VocabularyEntity
import co.com.jikanle.core.domain.model.Vocabulary

fun VocabularyEntity.toDomain(): Vocabulary = Vocabulary(
    id = id,
    term = term,
    reading = reading,
    meaning = meaning,
    example = example,
)

/** Standalone vocabulary always has an id; rows without one aren't cacheable. */
fun Vocabulary.toEntityOrNull(): VocabularyEntity? {
    val rowId = id ?: return null
    return VocabularyEntity(
        id = rowId,
        term = term,
        reading = reading,
        meaning = meaning,
        example = example,
    )
}
