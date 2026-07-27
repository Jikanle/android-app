package co.com.jikanle.core.data.repository

import android.content.Context
import co.com.jikanle.R
import co.com.jikanle.core.data.AppJson
import co.com.jikanle.core.di.IoDispatcher
import co.com.jikanle.core.domain.model.TranslatedSongDemo
import co.com.jikanle.core.domain.repository.TranslatedSongRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundledTranslatedSongRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TranslatedSongRepository {
    override suspend fun loadDemoSong(): Result<TranslatedSongDemo> = withContext(ioDispatcher) {
        runCatching {
            val json = context.resources.openRawResource(R.raw.songbridge_demo)
                .bufferedReader().use { it.readText() }
            AppJson.decodeFromString<TranslatedSongDemo>(json)
        }
    }

    // TODO(songbridge-supabase): replace this bundled source with PostgREST reads from
    // songs, song_lyric_lines, song_translations, song_translation_lines, and
    // song_vocabulary after the Phase 1 schema + Sakura seed are deployed.
}
