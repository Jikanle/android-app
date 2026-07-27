package co.com.jikanle.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import co.com.jikanle.core.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    /** The public, published Library (Build Brief §7.3), newest first. */
    @Query(
        "SELECT * FROM lessons WHERE visibility = 'public' AND publishedAt IS NOT NULL " +
            "ORDER BY publishedAt DESC",
    )
    fun observeLibrary(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    fun observeById(id: String): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE songId = :songId")
    fun observeBySong(songId: String): Flow<List<LessonEntity>>

    @Upsert
    suspend fun upsertAll(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons")
    suspend fun clear()
}
