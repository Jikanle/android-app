package co.com.jikanle.core.domain.repository

import co.com.jikanle.core.domain.model.DiscoverableEvent
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observePublicEvents(): Flow<List<DiscoverableEvent>>
    suspend fun refreshPublicEvents(): Result<Unit>
}
