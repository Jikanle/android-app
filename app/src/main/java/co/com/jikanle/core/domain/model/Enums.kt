package co.com.jikanle.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Skill level for a language. Stored lowercase in Postgres. */
@Serializable
enum class Level {
    @SerialName("beginner") BEGINNER,
    @SerialName("intermediate") INTERMEDIATE,
    @SerialName("advanced") ADVANCED,
}

/** A user can hold several roles at once (CLAUDE.md §7.1) — stored as a roles array. */
@Serializable
enum class Role {
    @SerialName("learner") LEARNER,
    @SerialName("creator") CREATOR,
    @SerialName("host") HOST,
    @SerialName("admin") ADMIN,
}

/** Lesson visibility (CLAUDE.md §6). */
@Serializable
enum class LessonVisibility {
    @SerialName("public") PUBLIC,
    @SerialName("event_only") EVENT_ONLY,
    @SerialName("unlisted") UNLISTED,
}

/** Companion match lifecycle (CLAUDE.md §6). */
@Serializable
enum class MatchStatus {
    @SerialName("pending") PENDING,
    @SerialName("accepted") ACCEPTED,
    @SerialName("declined") DECLINED,
}
