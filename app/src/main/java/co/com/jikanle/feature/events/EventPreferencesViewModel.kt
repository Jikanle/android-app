package co.com.jikanle.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.jikanle.core.domain.repository.AuthRepository
import co.com.jikanle.core.domain.repository.AuthState
import co.com.jikanle.core.domain.repository.EventPreferences
import co.com.jikanle.core.domain.repository.EventPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PreferenceMessage { None, Saved, Withdrawn, Failed }
data class EventPreferencesState(
    val signedIn: Boolean = false,
    val busy: Boolean = false,
    val loaded: Boolean = false,
    val preferences: EventPreferences = EventPreferences(),
    val message: PreferenceMessage = PreferenceMessage.None,
)

@HiltViewModel
class EventPreferencesViewModel @Inject constructor(
    private val repository: EventPreferencesRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(EventPreferencesState())
    val state = mutableState.asStateFlow()
    private var account: String? = null
    private var sessionGeneration = 0
    private var request: Job? = null

    init {
        viewModelScope.launch {
            auth.authState.collect { status ->
                val next = (status as? AuthState.Authenticated)?.userId
                if (next != account) {
                    sessionGeneration++
                    request?.cancel()
                    account = next
                    mutableState.value = EventPreferencesState(signedIn = next != null)
                    if (next != null) reload()
                }
            }
        }
    }

    fun edit(change: (EventPreferences) -> EventPreferences) {
        val current = state.value
        if (current.loaded && !current.busy) {
            mutableState.value = current.copy(preferences = change(current.preferences), message = PreferenceMessage.None)
        }
    }

    fun reload() = execute {
        state.value.copy(preferences = repository.load(), loaded = true)
    }

    fun save() {
        if (!state.value.loaded) return
        val values = state.value.preferences
        execute {
            repository.save(values)
            state.value.copy(message = PreferenceMessage.Saved)
        }
    }

    fun withdraw() = execute {
        repository.withdraw()
        state.value.copy(
            preferences = EventPreferences(), loaded = true, message = PreferenceMessage.Withdrawn,
        )
    }

    private fun execute(operation: suspend () -> EventPreferencesState) {
        if (state.value.busy || account == null || auth.currentUserId != account) return
        val generation = sessionGeneration
        mutableState.value = state.value.copy(busy = true, message = PreferenceMessage.None)
        request = viewModelScope.launch {
            try {
                val result = operation()
                if (generation == sessionGeneration && auth.currentUserId == account) mutableState.value = result
            }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) {
                if (generation == sessionGeneration) mutableState.value = state.value.copy(message = PreferenceMessage.Failed)
            }
            finally {
                if (generation == sessionGeneration) mutableState.value = state.value.copy(busy = false)
            }
        }
    }
}
