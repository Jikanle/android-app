package co.com.jikanle.core.data.repository

import co.com.jikanle.core.data.local.dao.ProfileDao
import co.com.jikanle.core.data.mapper.toDomain
import co.com.jikanle.core.data.mapper.toEntity
import co.com.jikanle.core.di.IoDispatcher
import co.com.jikanle.core.domain.model.User
import co.com.jikanle.core.domain.repository.ProfileRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val profileDao: ProfileDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ProfileRepository {

    override fun observeProfile(id: String): Flow<User?> =
        profileDao.observeById(id).map { it?.toDomain() }

    override suspend fun refreshProfile(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val user = postgrest.from("profiles")
                .select { filter { eq("id", id) } }
                .decodeSingleOrNull<User>()
            if (user != null) profileDao.upsert(user.toEntity())
        }
    }

    override suspend fun updateProfile(user: User): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            // explicitNulls = false means null fields are omitted, so generated columns
            // (created_at) and unset fields aren't clobbered. RLS enforces auth.uid() = id.
            postgrest.from("profiles").upsert(user)
            profileDao.upsert(user.toEntity())
        }
    }
}
