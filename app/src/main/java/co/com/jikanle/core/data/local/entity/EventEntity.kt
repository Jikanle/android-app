package co.com.jikanle.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val startsAt: String?,
    val endsAt: String?,
    val venueName: String?,
    val venueLocation: String?,
    val lumaUrl: String?,
    val coverImageUrl: String?,
    val ticketPriceCop: Int?,
    val organizationId: String?,
    val organizationName: String?,
    val communityUrl: String?,
    val language: String?,
    val level: String?,
    val summary: String?,
    val tags: List<String>,
    val timezone: String?,
    val lat: Double?,
    val lng: Double?,
)
