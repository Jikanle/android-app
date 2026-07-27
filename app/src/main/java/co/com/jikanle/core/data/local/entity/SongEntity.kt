package co.com.jikanle.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Offline cache row for [co.com.jikanle.core.domain.model.Song]. */
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val titleOriginal: String,
    val titleRomanized: String?,
    val artist: String,
    val language: String,
    val audioUrl: String?,
    val coverUrl: String?,
    val durationMs: Long?,
    val releaseYear: Int?,
    val lrcUrl: String?,
    val vocabularySeedIds: List<String>,
)
