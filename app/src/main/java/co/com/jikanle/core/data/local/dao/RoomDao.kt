package co.com.jikanle.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import co.com.jikanle.core.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    @Query("SELECT * FROM rooms ORDER BY createdAt")
    fun observeAll(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    fun observeById(id: String): Flow<RoomEntity?>

    @Upsert
    suspend fun upsertAll(rooms: List<RoomEntity>)

    @Query("DELETE FROM rooms")
    suspend fun clear()
}
