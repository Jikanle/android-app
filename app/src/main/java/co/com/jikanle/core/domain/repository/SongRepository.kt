package co.com.jikanle.core.domain.repository

import co.com.jikanle.core.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first access to the song catalog. Reads always come from the local cache (so the
 * UI works offline); [refreshSongs] pulls the latest from Supabase into that cache.
 */
interface SongRepository {

    fun observeSongs(): Flow<List<Song>>

    fun observeSong(id: String): Flow<Song?>

    /** Fetch from Supabase and replace the local cache. Errors are returned, not thrown. */
    suspend fun refreshSongs(): Result<Unit>
}
