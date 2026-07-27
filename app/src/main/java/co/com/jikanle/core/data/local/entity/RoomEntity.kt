package co.com.jikanle.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Offline cache row for [co.com.jikanle.core.domain.model.Room] (eventId null = Open Room). */
@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val id: String,
    val eventId: String?,
    val name: String,
    val description: String?,
    val memberIds: List<String>,
    val createdAt: String?,
)
