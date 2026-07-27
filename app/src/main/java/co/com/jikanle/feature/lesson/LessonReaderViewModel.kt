package co.com.jikanle.feature.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.jikanle.core.data.seed.FUYU_SEED_LESSON_ID
import co.com.jikanle.core.domain.model.Lesson
import co.com.jikanle.core.domain.repository.AuthRepository
import co.com.jikanle.core.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The Fuyu no Hanashi seed lesson — the canción de la noche for Event #0. */
const val FUYU_LESSON_ID = FUYU_SEED_LESSON_ID

@HiltViewModel
class LessonReaderViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    /** True while the first online fetch is in flight (cache may be empty before it lands). */
    private val _refreshing = MutableStateFlow(true)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val lesson: StateFlow<Lesson?> = lessonRepository.observeLesson(FUYU_LESSON_ID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            lessonRepository.refreshLessons()
            _refreshing.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
