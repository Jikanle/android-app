package co.com.jikanle.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.com.jikanle.core.domain.model.Level
import co.com.jikanle.core.domain.model.Role

/** Offline cache row for [co.com.jikanle.core.domain.model.User] (the `profiles` table). */
@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val avatarUrl: String?,
    val nativeLanguages: List<String>,
    val targetLanguages: List<String>,
    val levelByLanguage: Map<String, Level>,
    val hobbies: List<String>,
    val roles: List<Role>,
    val joinedEventIds: List<String>,
    val createdAt: String?,
)
