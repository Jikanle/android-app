package co.com.jikanle.core.data.repository

import co.com.jikanle.BuildConfig
import co.com.jikanle.core.data.local.dao.EventDao
import co.com.jikanle.core.data.mapper.toDiscoverableEvent
import co.com.jikanle.core.data.mapper.toEntity
import co.com.jikanle.core.di.IoDispatcher
import co.com.jikanle.core.domain.model.DiscoverableEvent
import co.com.jikanle.core.domain.model.Event
import co.com.jikanle.core.domain.model.Organization
import co.com.jikanle.core.domain.repository.EventRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val postgrest: Lazy<Postgrest>,
    private val eventDao: EventDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : EventRepository {
    override fun observePublicEvents(): Flow<List<DiscoverableEvent>> =
        eventDao.observeAll().map { events -> events.map { it.toDiscoverableEvent() } }

    override suspend fun refreshPublicEvents(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_ANON_KEY.isBlank()) return@runCatching
            val organizations = postgrest.get().from("organizations").select().decodeList<Organization>()
                .associateBy { it.id }
            val events = postgrest.get().from("events")
                .select { filter { eq("visibility", "public") } }
                .decodeList<Event>()
                .sortedBy { it.startsAt ?: "9999" }
            eventDao.replaceAll(events.map { it.toEntity(organizations[it.organizationId]) })
        }
    }
}
