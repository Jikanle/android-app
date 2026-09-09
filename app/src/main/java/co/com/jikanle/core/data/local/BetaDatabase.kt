package co.com.jikanle.core.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "lesson_progress")
data class ProgressRow(@PrimaryKey val lessonId: String, val step: Int, val completed: Boolean)

@Entity(tableName = "beta_outbox")
data class OutboxRow(@PrimaryKey val id: String, val userId: String, val payload: String, val createdAt: Long)

@Dao
interface BetaDao {
    @Query("SELECT * FROM lesson_progress WHERE lessonId = :id")
    fun progress(id: String): Flow<ProgressRow?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(row: ProgressRow)

    @Insert
    suspend fun enqueue(row: OutboxRow)

    @Query("SELECT * FROM beta_outbox WHERE userId = :userId ORDER BY createdAt LIMIT 50")
    suspend fun pending(userId: String): List<OutboxRow>

    @Query("DELETE FROM beta_outbox WHERE id = :id")
    suspend fun acknowledge(id: String)

    @Query("DELETE FROM beta_outbox")
    suspend fun clearOutbox()

    @Query("DELETE FROM beta_outbox WHERE id NOT IN (SELECT id FROM beta_outbox ORDER BY createdAt DESC LIMIT 500)")
    suspend fun trimOutbox()
}

/** Kept apart from the disposable content cache so cache upgrades preserve progress. */
@Database(entities = [ProgressRow::class, OutboxRow::class], version = 1, exportSchema = false)
abstract class BetaDatabase : RoomDatabase() {
    abstract fun betaDao(): BetaDao
}
