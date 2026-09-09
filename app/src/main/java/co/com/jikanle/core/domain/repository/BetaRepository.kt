package co.com.jikanle.core.domain.repository

import kotlinx.coroutines.flow.Flow

enum class ProductEvent(val wireName: String) {
    AppOpened("app_opened"),
    LessonOpened("lesson_opened"),
    LessonStarted("lesson_started"),
    LessonCompleted("lesson_completed"),
    VocabularyOpened("vocabulary_opened"),
    EventRsvpOpened("event_rsvp_opened"),
    CommunityOpened("community_opened"),
    FeedbackSubmitted("feedback_submitted"),
}

/** Device-local lesson position. Completion is self-reported, not a learning score. */
data class LessonProgress(val step: Int = 0, val completed: Boolean = false)

interface BetaRepository {
    val sharingEnabled: Flow<Boolean>
    fun progress(lessonId: String): Flow<LessonProgress>
    suspend fun saveProgress(lessonId: String, progress: LessonProgress)
    suspend fun setSharing(enabled: Boolean)
    suspend fun track(event: ProductEvent, lessonId: String? = null)
    suspend fun submitFeedback(rating: Int): Boolean
    suspend fun flush(): Boolean
}
