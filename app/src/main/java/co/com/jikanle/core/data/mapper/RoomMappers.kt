package co.com.jikanle.core.data.mapper

import co.com.jikanle.core.data.local.entity.RoomEntity
import co.com.jikanle.core.domain.model.Room

fun RoomEntity.toDomain(): Room = Room(
    id = id,
    eventId = eventId,
    name = name,
    description = description,
    memberIds = memberIds,
    createdAt = createdAt,
)

fun Room.toEntity(): RoomEntity = RoomEntity(
    id = id,
    eventId = eventId,
    name = name,
    description = description,
    memberIds = memberIds,
    createdAt = createdAt,
)
