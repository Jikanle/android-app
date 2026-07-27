package co.com.jikanle.core.domain.repository

import co.com.jikanle.core.domain.model.Vocabulary
import kotlinx.coroutines.flow.Flow

/** Offline-first access to standalone Vocabulary (song seeds). */
interface VocabularyRepository {

    fun observeVocabulary(ids: List<String>): Flow<List<Vocabulary>>

    suspend fun refreshVocabulary(): Result<Unit>
}
