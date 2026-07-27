package co.com.jikanle.core.domain.repository

import co.com.jikanle.core.domain.model.Room
import kotlinx.coroutines.flow.Flow

/** Offline-first access to Rooms (joined event rooms + the global Open Room). */
interface RoomRepository {

    fun observeRooms(): Flow<List<Room>>

    fun observeRoom(id: String): Flow<Room?>

    suspend fun refreshRooms(): Result<Unit>
}
