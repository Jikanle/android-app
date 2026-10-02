package co.com.jikanle.core.data.repository

import android.content.Context
import android.net.Uri
import co.com.jikanle.BuildConfig
import kotlinx.coroutines.CancellationException
import co.com.jikanle.R
import co.com.jikanle.core.data.AppJson
import co.com.jikanle.core.di.IoDispatcher
import co.com.jikanle.core.domain.model.TranslatedSongDemo
import co.com.jikanle.core.domain.model.normalized
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
            AppJson.decodeFromString<TranslatedSongDemo>(json).normalized()
        }
    }

    override suspend fun loadLocalStudy(uri: String): Result<TranslatedSongDemo> = withContext(ioDispatcher) {
        try {
            check(BuildConfig.DEBUG) { "Local research imports are debug-only" }
            val parsed = Uri.parse(uri)
            require(parsed.scheme == "content")
            val bytes = requireNotNull(context.contentResolver.openInputStream(parsed)).use {
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val count = it.read(buffer)
                    if (count < 0) break
                    require(output.size() + count <= 262_144) { "Study exceeds 256 KiB" }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            require(bytes.size <= 262_144) { "Study exceeds 256 KiB" }
            Result.success(AppJson.decodeFromString<TranslatedSongDemo>(bytes.toString(Charsets.UTF_8)).normalized())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    // TODO(songbridge-supabase): replace this bundled source with PostgREST reads from
    // songs, song_lyric_lines, song_translations, song_translation_lines, and
    // song_vocabulary after the Phase 1 schema + Sakura seed are deployed.
}
