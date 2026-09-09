package co.com.jikanle.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.jikanle.core.domain.repository.AuthRepository
import co.com.jikanle.core.domain.repository.AuthState
import co.com.jikanle.core.domain.repository.BetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Delivery { Idle, Sending, Sent, Pending, Failed }

@HiltViewModel
class BetaViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val beta: BetaRepository,
) : ViewModel() {
    val authState = auth.authState.stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Loading)
    val sharing = beta.sharingEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val delivery = MutableStateFlow(Delivery.Idle)
    val busy = MutableStateFlow(false)

    fun share(enabled: Boolean) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try { beta.setSharing(enabled) }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { delivery.value = Delivery.Failed }
            finally { busy.value = false }
        }
    }

    fun submit(rating: Int) {
        if (delivery.value == Delivery.Sending || delivery.value == Delivery.Pending) return
        delivery.value = Delivery.Sending
        viewModelScope.launch {
            try {
                delivery.value = if (!beta.submitFeedback(rating)) Delivery.Failed
                else if (beta.flush()) Delivery.Sent else Delivery.Pending
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) { delivery.value = Delivery.Failed }
        }
    }

    fun retry() {
        if (delivery.value == Delivery.Sending) return
        delivery.value = Delivery.Sending
        viewModelScope.launch { delivery.value = if (beta.flush()) Delivery.Sent else Delivery.Pending }
    }

    fun signOut() {
        viewModelScope.launch {
            beta.setSharing(false)
            auth.signOut()
            delivery.value = Delivery.Idle
        }
    }
}
