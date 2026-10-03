package co.com.jikanle.core.domain.repository

data class EventPreferences(
    val languages: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val domains: List<String> = emptyList(),
    val goals: List<String> = emptyList(),
    val matching: Boolean = false,
    val metrics: Boolean = false,
)

interface EventPreferencesRepository {
    suspend fun load(): EventPreferences
    suspend fun save(preferences: EventPreferences)
    suspend fun withdraw()
}
