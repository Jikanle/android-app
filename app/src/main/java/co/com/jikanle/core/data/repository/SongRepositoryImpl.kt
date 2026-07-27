package co.com.jikanle.core.data.repository

import co.com.jikanle.core.data.local.dao.SongDao
import co.com.jikanle.core.data.mapper.toDomain
import co.com.jikanle.core.data.mapper.toEntity
import co.com.jikanle.core.di.IoDispatcher
import co.com.jikanle.core.domain.model.Song
import co.com.jikanle.core.domain.repository.SongRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val songDao: SongDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SongRepository {

    override fun observeSongs(): Flow<List<Song>> =
        songDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeSong(id: String): Flow<Song?> =
        songDao.observeById(id).map { it?.toDomain() }

    override suspend fun refreshSongs(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val songs = postgrest.from("songs").select().decodeList<Song>()
            songDao.upsertAll(songs.map { it.toEntity() })
        }
    }
}
