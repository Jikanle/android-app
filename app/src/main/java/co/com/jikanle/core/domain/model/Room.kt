package co.com.jikanle.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The continuity space for a group who attended together — or the global Open Room
 * when [eventId] is null (CLAUDE.md §6, §10 P0 item 6).
 */
@Serializable
data class Room(
    val id: String,
    @SerialName("event_id") val eventId: String? = null,
    val name: String,
    val description: String? = null,
    @SerialName("member_ids") val memberIds: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null,
) {
    /** The single global room every authenticated user is auto-joined to. */
    val isOpenRoom: Boolean get() = eventId == null
}
