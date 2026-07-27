package co.com.jikanle.core.data.mapper

import co.com.jikanle.core.data.local.entity.ProfileEntity
import co.com.jikanle.core.domain.model.User

fun ProfileEntity.toDomain(): User = User(
    id = id,
    displayName = displayName,
    avatarUrl = avatarUrl,
    nativeLanguages = nativeLanguages,
    targetLanguages = targetLanguages,
    levelByLanguage = levelByLanguage,
    hobbies = hobbies,
    roles = roles,
    joinedEventIds = joinedEventIds,
    createdAt = createdAt,
)

fun User.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    displayName = displayName,
    avatarUrl = avatarUrl,
    nativeLanguages = nativeLanguages,
    targetLanguages = targetLanguages,
    levelByLanguage = levelByLanguage,
    hobbies = hobbies,
    roles = roles,
    joinedEventIds = joinedEventIds,
    createdAt = createdAt,
)
