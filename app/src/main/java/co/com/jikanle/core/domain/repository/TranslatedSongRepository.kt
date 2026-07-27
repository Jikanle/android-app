package co.com.jikanle.core.domain.repository

import co.com.jikanle.core.domain.model.TranslatedSongDemo

interface TranslatedSongRepository {
    suspend fun loadDemoSong(): Result<TranslatedSongDemo>
}
