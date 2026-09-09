package co.com.jikanle.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.jikanle.core.domain.model.DiscoverableEvent
import co.com.jikanle.core.domain.repository.EventRepository
import co.com.jikanle.core.domain.repository.BetaRepository
import co.com.jikanle.core.domain.repository.ProductEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EventsUiState {
    data object Loading : EventsUiState
    data class Content(val events: List<DiscoverableEvent>, val refreshing: Boolean) : EventsUiState
    data object Error : EventsUiState
}

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val beta: BetaRepository,
) : ViewModel() {
    private val refreshInProgress = MutableStateFlow(true)
    private val refreshFailed = MutableStateFlow(false)
    private var refreshJob: kotlinx.coroutines.Job? = null

    val state: StateFlow<EventsUiState> = combine(
        eventRepository.observePublicEvents(),
        refreshInProgress,
        refreshFailed,
    ) { events, refreshing, failed ->
            when {
                events.isNotEmpty() -> EventsUiState.Content(events, refreshing)
                refreshing -> EventsUiState.Loading
                failed -> EventsUiState.Error
                else -> EventsUiState.Content(emptyList(), refreshing = false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventsUiState.Loading)

    init { refresh() }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshInProgress.value = true
        refreshFailed.value = false
        refreshJob = viewModelScope.launch {
            eventRepository.refreshPublicEvents().onFailure {
                refreshFailed.value = true
            }.onSuccess {
                refreshFailed.value = false
            }
            refreshInProgress.value = false
        }
    }

    fun track(event: ProductEvent) {
        viewModelScope.launch { beta.track(event); beta.flush() }
    }
}
