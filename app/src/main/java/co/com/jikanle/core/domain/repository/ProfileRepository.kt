package co.com.jikanle.core.domain.repository

import co.com.jikanle.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/** Offline-first access to the user's own profile (read + edit, Build Brief §10 P0 #5). */
interface ProfileRepository {

    fun observeProfile(id: String): Flow<User?>

    suspend fun refreshProfile(id: String): Result<Unit>

    /** Persist edits (display name, languages, level, hobbies) to Supabase + cache. */
    suspend fun updateProfile(user: User): Result<Unit>
}
