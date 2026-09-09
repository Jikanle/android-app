package co.com.jikanle.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.jikanle.core.domain.repository.AuthRepository
import co.com.jikanle.core.domain.repository.AuthState
import co.com.jikanle.core.domain.repository.BetaRepository
import co.com.jikanle.core.domain.repository.ProductEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BetaSessionViewModel @Inject constructor(private val auth: AuthRepository, private val beta: BetaRepository) : ViewModel() {
    private val recordedUsers = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            combine(auth.authState, beta.sharingEnabled) { state, sharing -> state to sharing }.collect { (state, sharing) ->
                if (state is AuthState.Authenticated) {
                    if (sharing) recordOpen(state.userId)
                    beta.flush()
                }
            }
        }
    }

    fun foreground() {
        viewModelScope.launch {
            val userId = auth.currentUserId ?: return@launch
            if (beta.sharingEnabled.first()) recordOpen(userId)
            beta.flush()
        }
    }

    private suspend fun recordOpen(userId: String) {
        val day = java.time.LocalDate.now(java.time.ZoneId.of("America/Bogota"))
        if (recordedUsers.add("$userId:$day")) beta.track(ProductEvent.AppOpened)
    }
}
