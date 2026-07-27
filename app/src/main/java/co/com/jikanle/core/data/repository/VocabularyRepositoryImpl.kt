package co.com.jikanle.core.data.repository

import co.com.jikanle.core.data.local.dao.VocabularyDao
import co.com.jikanle.core.data.mapper.toDomain
import co.com.jikanle.core.data.mapper.toEntityOrNull
import co.com.jikanle.core.di.IoDispatcher
import co.com.jikanle.core.domain.model.Vocabulary
import co.com.jikanle.core.domain.repository.VocabularyRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val vocabularyDao: VocabularyDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VocabularyRepository {

    override fun observeVocabulary(ids: List<String>): Flow<List<Vocabulary>> =
        vocabularyDao.observeByIds(ids).map { rows -> rows.map { it.toDomain() } }

    override suspend fun refreshVocabulary(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val items = postgrest.from("vocabulary").select().decodeList<Vocabulary>()
            vocabularyDao.upsertAll(items.mapNotNull { it.toEntityOrNull() })
        }
    }
}
