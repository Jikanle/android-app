package co.com.jikanle.core.data.repository

import co.com.jikanle.BuildConfig
import co.com.jikanle.core.data.local.dao.LessonDao
import co.com.jikanle.core.data.mapper.toDomain
import co.com.jikanle.core.data.mapper.toEntity
import co.com.jikanle.core.data.seed.FUYU_SEED_LESSON_ID
import co.com.jikanle.core.data.seed.SeedLessonDataSource
import co.com.jikanle.core.di.IoDispatcher
import co.com.jikanle.core.domain.model.Lesson
import co.com.jikanle.core.domain.repository.LessonRepository
import dagger.Lazy
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val postgrest: Lazy<Postgrest>,
    private val lessonDao: LessonDao,
    private val seedLessonDataSource: SeedLessonDataSource,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LessonRepository {

    override fun observeLibrary(): Flow<List<Lesson>> =
        lessonDao.observeLibrary().map { rows ->
            rows.map { it.toDomain() }.ifEmpty {
                seedLessonDataSource.loadFuyuLesson().map(::listOf).getOrDefault(emptyList())
            }
        }

    override fun observeLesson(id: String): Flow<Lesson?> =
        lessonDao.observeById(id).map { row ->
            row?.toDomain() ?: if (id == FUYU_SEED_LESSON_ID) {
                seedLessonDataSource.loadFuyuLesson().getOrNull()
            } else {
                null
            }
        }

    override fun observeLessonsForSong(songId: String): Flow<List<Lesson>> =
        lessonDao.observeBySong(songId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun refreshLessons(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            seedLessonDataSource.loadFuyuLesson()
                .getOrNull()
                ?.let { lessonDao.upsertAll(listOf(it.toEntity())) }

            if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_ANON_KEY.isBlank()) {
                return@runCatching
            }

            val lessons = postgrest.get().from("lessons").select().decodeList<Lesson>()
            lessonDao.upsertAll(lessons.map { it.toEntity() })
        }
    }
}
