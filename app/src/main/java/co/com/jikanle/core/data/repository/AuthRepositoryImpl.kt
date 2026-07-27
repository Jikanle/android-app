package co.com.jikanle.core.data.repository

import co.com.jikanle.core.di.IoDispatcher
import co.com.jikanle.core.domain.repository.AuthRepository
import co.com.jikanle.core.domain.repository.AuthState
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AuthRepository {

    override val authState: Flow<AuthState> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> AuthState.Authenticated(status.session.user?.id.orEmpty())
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
            is SessionStatus.RefreshFailure -> AuthState.Unauthenticated
        }
    }

    override val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                Unit
            }
        }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
            }
        }

    override suspend fun signInWithGoogle(): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { auth.signInWith(Google) }
        }

    override suspend fun signOut(): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { auth.signOut() }
        }
}
