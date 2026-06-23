package co.com.jikanle.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Jikanle member (CLAUDE.md §6). Field names map to the shared Postgres schema
 * via [SerialName] so the same rows serve web and Android.
 */
@Serializable
data class User(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("native_languages") val nativeLanguages: List<String> = emptyList(),
    @SerialName("target_languages") val targetLanguages: List<String> = emptyList(),
    @SerialName("level_by_language") val levelByLanguage: Map<String, Level> = emptyMap(),
    val hobbies: List<String> = emptyList(),
    val roles: List<Role> = listOf(Role.LEARNER),
    @SerialName("joined_event_ids") val joinedEventIds: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null,
)
