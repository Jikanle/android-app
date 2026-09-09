package co.com.jikanle.core.data.repository

import android.content.Context
import co.com.jikanle.BuildConfig
import co.com.jikanle.core.data.AppJson
import co.com.jikanle.core.data.local.BetaDao
import co.com.jikanle.core.data.local.OutboxRow
import co.com.jikanle.core.data.local.ProgressRow
import co.com.jikanle.core.di.IoDispatcher
import co.com.jikanle.core.domain.repository.AuthRepository
import co.com.jikanle.core.domain.repository.BetaRepository
import co.com.jikanle.core.domain.repository.LessonProgress
import co.com.jikanle.core.domain.repository.ProductEvent
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
internal data class BetaEventDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("event_name") val eventName: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("lesson_id") val lessonId: String? = null,
    val language: String,
    @SerialName("occurred_at") val occurredAt: String,
    val rating: Int? = null,
)

@Singleton
class BetaRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dao: BetaDao,
    private val auth: AuthRepository,
    private val postgrest: Lazy<Postgrest>,
    @param:IoDispatcher private val io: CoroutineDispatcher,
) : BetaRepository {
    private val preferences = context.getSharedPreferences("beta_preferences", Context.MODE_PRIVATE)
    override val sharingEnabled = MutableStateFlow(preferences.getBoolean("share_usage", false))
    private val mutex = Mutex()
    private val sessionId = UUID.randomUUID().toString()

    override fun progress(lessonId: String) = dao.progress(lessonId).map {
        LessonProgress(it?.step ?: 0, it?.completed ?: false)
    }

    override suspend fun saveProgress(lessonId: String, progress: LessonProgress) {
        dao.save(ProgressRow(lessonId, progress.step.coerceAtLeast(0), progress.completed))
    }

    override suspend fun setSharing(enabled: Boolean) = withContext(io) {
        mutex.withLock {
            if (!enabled) dao.clearOutbox()
            check(preferences.edit().putBoolean("share_usage", enabled).commit())
            sharingEnabled.value = enabled
        }
    }

    override suspend fun track(event: ProductEvent, lessonId: String?) {
        try {
            enqueue(event, lessonId, null, requireSharing = true)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // A full disk or unavailable outbox must not interrupt the lesson.
        }
    }

    override suspend fun submitFeedback(rating: Int): Boolean {
        require(rating in 1..5)
        return enqueue(ProductEvent.FeedbackSubmitted, null, rating, requireSharing = false)
    }

    private suspend fun enqueue(event: ProductEvent, lessonId: String?, rating: Int?, requireSharing: Boolean): Boolean = withContext(io) {
        mutex.withLock {
            val userId = auth.currentUserId ?: return@withLock false
            if (requireSharing && !sharingEnabled.value) return@withLock false
            val payload = BetaEventDto(
                id = UUID.randomUUID().toString(), userId = userId, sessionId = sessionId,
                eventName = event.wireName, appVersion = BuildConfig.VERSION_NAME,
                lessonId = lessonId, language = Locale.getDefault().language,
                occurredAt = Instant.now().toString(), rating = rating,
            )
            dao.enqueue(OutboxRow(payload.id, userId, AppJson.encodeToString(BetaEventDto.serializer(), payload), System.currentTimeMillis()))
            dao.trimOutbox()
            true
        }
    }

    override suspend fun flush(): Boolean = withContext(io) {
        mutex.withLock {
            val userId = auth.currentUserId ?: return@withLock false
            try {
                val rows = dao.pending(userId)
                for (row in rows) {
                    if (auth.currentUserId != userId) return@withLock false
                    // Stable UUID plus ignoreDuplicates makes retries safe after a lost response.
                    postgrest.get().from("beta_events").upsert(AppJson.decodeFromString<BetaEventDto>(row.payload)) {
                        onConflict = "id"
                        ignoreDuplicates = true
                    }
                    dao.acknowledge(row.id)
                }
                dao.pending(userId).isEmpty()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                false
            }
        }
    }
}
