package co.com.jikanle.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import co.com.jikanle.core.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    @Query("SELECT * FROM vocabulary WHERE id IN (:ids)")
    fun observeByIds(ids: List<String>): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary")
    fun observeAll(): Flow<List<VocabularyEntity>>

    @Upsert
    suspend fun upsertAll(items: List<VocabularyEntity>)

    @Query("DELETE FROM vocabulary")
    suspend fun clear()
}
