package co.com.jikanle.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Jikanle evening (CLAUDE.md §6). The physical "Room" people attend; [roomId] is the
 * 1:1 continuity [Room] created for it. [featuredSongId] is the *canción de la noche*;
 * [playlistSongIds] the background playlist. [ticketPriceCop] null => free entry.
 */
@Serializable
data class Event(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("host_id") val hostId: String? = null,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("venue_name") val venueName: String? = null,
    @SerialName("venue_location") val venueLocation: String? = null,
    @SerialName("luma_url") val lumaUrl: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("featured_song_id") val featuredSongId: String? = null,
    @SerialName("playlist_song_ids") val playlistSongIds: List<String> = emptyList(),
    @SerialName("room_id") val roomId: String? = null,
    @SerialName("ticket_price_cop") val ticketPriceCop: Int? = null,
    @SerialName("max_attendees") val maxAttendees: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
