package co.com.jikanle.feature.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.com.jikanle.core.data.seed.FUYU_SEED_LESSON_ID
import co.com.jikanle.core.domain.model.Lesson
import co.com.jikanle.core.domain.repository.BetaRepository
import co.com.jikanle.core.domain.repository.LessonProgress
import co.com.jikanle.core.domain.repository.ProductEvent
import co.com.jikanle.core.domain.model.VocabularySlide
import co.com.jikanle.core.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The Fuyu no Hanashi seed lesson — the canción de la noche for Event #0. */
const val FUYU_LESSON_ID = FUYU_SEED_LESSON_ID

@HiltViewModel
class LessonReaderViewModel @Inject constructor(
    state: SavedStateHandle,
    private val lessonRepository: LessonRepository,
    private val beta: BetaRepository,
) : ViewModel() {

    val lessonId: String = state["lessonId"] ?: FUYU_LESSON_ID
    val progress = beta.progress(lessonId).stateIn(viewModelScope, SharingStarted.Eagerly, LessonProgress())
    val saving = MutableStateFlow(false)
    val saveFailed = MutableStateFlow(false)

    /** True while the first online fetch is in flight (cache may be empty before it lands). */
    private val _refreshing = MutableStateFlow(true)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val lesson: StateFlow<Lesson?> = lessonRepository.observeLesson(lessonId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        refresh()
        viewModelScope.launch { beta.track(ProductEvent.LessonOpened, lessonId); beta.flush() }
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            try { lessonRepository.refreshLessons() } finally { _refreshing.value = false }
        }
    }

    fun next() = updateProgress { current, count -> advanceLesson(current, count) }
    fun previous() = updateProgress { current, _ -> current.copy(step = (current.step - 1).coerceAtLeast(0)) }
    fun restart() = updateProgress { _, _ -> LessonProgress() }

    private fun updateProgress(change: (LessonProgress, Int) -> LessonProgress) {
        if (saving.value) return
        val content = lesson.value ?: return
        saving.value = true
        viewModelScope.launch {
            try {
                // Read storage before writing so a slow initial restore cannot reset progress.
                val old = beta.progress(lessonId).first()
                val next = change(old, content.slideDeck.slides.size)
                beta.saveProgress(lessonId, next)
                saveFailed.value = false
                if (!old.completed && next.completed) beta.track(ProductEvent.LessonCompleted, lessonId)
                if (old.step == 0 && next.step == 1) beta.track(ProductEvent.LessonStarted, lessonId)
                if (next.step != old.step && content.slideDeck.slides.getOrNull(next.step) is VocabularySlide) {
                    beta.track(ProductEvent.VocabularyOpened, lessonId)
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                saveFailed.value = true
            } finally { saving.value = false }
            beta.flush()
        }
    }
}
