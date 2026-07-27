package co.com.jikanle.core.domain.repository

import co.com.jikanle.core.domain.model.Lesson
import kotlinx.coroutines.flow.Flow

/** Offline-first access to Lessons (reader + Library). */
interface LessonRepository {

    /** Public, published Lessons for the Library (Build Brief §7.3). */
    fun observeLibrary(): Flow<List<Lesson>>

    fun observeLesson(id: String): Flow<Lesson?>

    fun observeLessonsForSong(songId: String): Flow<List<Lesson>>

    /** Fetch every Lesson the current user may see (RLS-scoped) into the cache. */
    suspend fun refreshLessons(): Result<Unit>
}
