package co.com.jikanle.core.data.repository

import co.com.jikanle.core.domain.repository.AuthRepository
import co.com.jikanle.core.domain.repository.EventPreferences
import co.com.jikanle.core.domain.repository.EventPreferencesRepository
import dagger.Lazy
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.add
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class EventPreferencesDto(
    val languages: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    @SerialName("professional_domains") val domains: List<String> = emptyList(),
    val goals: List<String> = emptyList(),
    @SerialName("matching_opt_in") val matching: Boolean = false,
    @SerialName("metrics_opt_in") val metrics: Boolean = false,
) {
    fun toDomain() = EventPreferences(languages, interests, domains, goals, matching, metrics)
}

@Singleton
class EventPreferencesRepositoryImpl @Inject constructor(
    private val postgrest: Lazy<Postgrest>,
    private val auth: AuthRepository,
) : EventPreferencesRepository {
    override suspend fun load(): EventPreferences {
        val user = checkNotNull(auth.currentUserId)
        return postgrest.get().from("event_preferences").select {
            filter { eq("user_id", user) }
        }.decodeSingleOrNull<EventPreferencesDto>()?.toDomain() ?: EventPreferences()
    }

    override suspend fun save(preferences: EventPreferences) {
        checkNotNull(auth.currentUserId)
        postgrest.get().rpc("save_event_preferences", buildJsonObject {
            putJsonArray("p_languages") { preferences.languages.forEach { add(it) } }
            putJsonArray("p_interests") { preferences.interests.forEach { add(it) } }
            putJsonArray("p_domains") { preferences.domains.forEach { add(it) } }
            putJsonArray("p_goals") { preferences.goals.forEach { add(it) } }
            put("p_matching", preferences.matching)
            put("p_metrics", preferences.metrics)
        })
    }

    override suspend fun withdraw() {
        checkNotNull(auth.currentUserId)
        postgrest.get().rpc("withdraw_event_preferences")
    }
}
