package co.com.jikanle.core.domain.repository

import kotlinx.coroutines.flow.Flow

/** The signed-in state of the app, derived from Supabase's session status. */
sealed interface AuthState {
    /** Session is still being restored from storage — show a splash, don't decide yet. */
    data object Loading : AuthState
    data class Authenticated(val userId: String) : AuthState
    data object Unauthenticated : AuthState
}

/**
 * Auth against the shared Supabase project. Email/password works once "Confirm email" is
 * turned OFF in Supabase (the project currently has it ON).
 *
 * OAuth (Google) launches a Custom Tab to the provider; the session comes back via
 * the `jikanle://auth-callback` deep link, which [co.com.jikanle.MainActivity] feeds to
 * `handleDeeplinks`. The provider still needs enabling in the Supabase dashboard + its
 * client IDs/secrets before the round-trip succeeds at runtime.
 */
interface AuthRepository {

    val authState: Flow<AuthState>

    val currentUserId: String?

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>

    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    /** Launches the Google OAuth Custom Tab. Completion arrives via the auth-callback deep link. */
    suspend fun signInWithGoogle(): Result<Unit>

    suspend fun signOut(): Result<Unit>
}
