package co.com.jikanle.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A song that Lessons attach to (CLAUDE.md §6). [lrcUrl] points at an LRC file in the
 * Supabase `lyrics/` bucket; [audioUrl] at the `songs/` bucket.
 */
@Serializable
data class Song(
    val id: String,
    @SerialName("title_original") val titleOriginal: String,
    @SerialName("title_romanized") val titleRomanized: String? = null,
    val artist: String,
    val language: String,
    @SerialName("audio_url") val audioUrl: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("duration_ms") val durationMs: Long? = null,
    @SerialName("release_year") val releaseYear: Int? = null,
    @SerialName("lrc_url") val lrcUrl: String? = null,
    @SerialName("vocabulary_seed_ids") val vocabularySeedIds: List<String> = emptyList(),
)
