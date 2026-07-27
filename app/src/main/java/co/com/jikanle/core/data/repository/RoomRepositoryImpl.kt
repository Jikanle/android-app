package co.com.jikanle.core.data.repository

import co.com.jikanle.core.data.local.dao.RoomDao
import co.com.jikanle.core.data.mapper.toDomain
import co.com.jikanle.core.data.mapper.toEntity
import co.com.jikanle.core.di.IoDispatcher
import co.com.jikanle.core.domain.model.Room
import co.com.jikanle.core.domain.repository.RoomRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val roomDao: RoomDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RoomRepository {

    override fun observeRooms(): Flow<List<Room>> =
        roomDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeRoom(id: String): Flow<Room?> =
        roomDao.observeById(id).map { it?.toDomain() }

    override suspend fun refreshRooms(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val rooms = postgrest.from("rooms").select().decodeList<Room>()
            roomDao.upsertAll(rooms.map { it.toEntity() })
        }
    }
}
