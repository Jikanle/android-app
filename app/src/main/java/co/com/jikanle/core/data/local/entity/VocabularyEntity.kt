package co.com.jikanle.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Offline cache row for a standalone [co.com.jikanle.core.domain.model.Vocabulary] (a song
 * seed). Only standalone rows are cached, so [id] is non-null here even though the domain
 * model's id is nullable (lesson-embedded picks aren't stored in this table).
 */
@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey val id: String,
    val term: String,
    val reading: String?,
    val meaning: String,
    val example: String?,
)
