package co.com.jikanle.core.data.mapper

import co.com.jikanle.core.data.local.entity.SongEntity
import co.com.jikanle.core.domain.model.Song

fun SongEntity.toDomain(): Song = Song(
    id = id,
    titleOriginal = titleOriginal,
    titleRomanized = titleRomanized,
    artist = artist,
    language = language,
    audioUrl = audioUrl,
    coverUrl = coverUrl,
    durationMs = durationMs,
    releaseYear = releaseYear,
    lrcUrl = lrcUrl,
    vocabularySeedIds = vocabularySeedIds,
)

fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    titleOriginal = titleOriginal,
    titleRomanized = titleRomanized,
    artist = artist,
    language = language,
    audioUrl = audioUrl,
    coverUrl = coverUrl,
    durationMs = durationMs,
    releaseYear = releaseYear,
    lrcUrl = lrcUrl,
    vocabularySeedIds = vocabularySeedIds,
)
