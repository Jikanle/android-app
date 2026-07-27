package co.com.jikanle.feature.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.jikanle.R
import co.com.jikanle.core.domain.repository.AuthRepository
import co.com.jikanle.core.domain.repository.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isSignUp: Boolean = false,
    val isBusy: Boolean = false,
    @param:StringRes val messageRes: Int? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value.trim(), messageRes = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, messageRes = null) }
    fun toggleMode() = _uiState.update { it.copy(isSignUp = !it.isSignUp, messageRes = null) }

    fun signInWithGoogle() = launchOAuth { authRepository.signInWithGoogle() }

    private fun launchOAuth(action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, messageRes = null) }
            val result = action()
            // On success a Custom Tab opens and the session returns via deep link, so we just
            // clear the busy flag; only surface a message if launching the provider failed.
            _uiState.update {
                it.copy(
                    isBusy = false,
                    messageRes = result.exceptionOrNull()?.let { R.string.auth_error_provider },
                )
            }
        }
    }

    fun submit() {
        val s = _uiState.value
        if (s.email.isBlank() || s.password.length < 6) {
            _uiState.update { it.copy(messageRes = R.string.auth_error_email_password) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, messageRes = null) }
            val result = if (s.isSignUp) {
                authRepository.signUpWithEmail(s.email, s.password)
            } else {
                authRepository.signInWithEmail(s.email, s.password)
            }
            _uiState.update {
                it.copy(
                    isBusy = false,
                    messageRes = result.exceptionOrNull()?.let { R.string.auth_error_generic }
                        ?: if (s.isSignUp) R.string.auth_account_created else null,
                )
            }
        }
    }
}
