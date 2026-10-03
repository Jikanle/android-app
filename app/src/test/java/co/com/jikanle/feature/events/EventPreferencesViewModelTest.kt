package co.com.jikanle.feature.events

import co.com.jikanle.core.domain.repository.AuthRepository
import co.com.jikanle.core.domain.repository.AuthState
import co.com.jikanle.core.domain.repository.EventPreferences
import co.com.jikanle.core.domain.repository.EventPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventPreferencesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    private class Auth : AuthRepository {
        override val authState = MutableStateFlow<AuthState>(AuthState.Authenticated("a"))
        override val currentUserId get() = (authState.value as? AuthState.Authenticated)?.userId
        override suspend fun signUpWithEmail(email: String, password: String) = Result.success(Unit)
        override suspend fun signInWithEmail(email: String, password: String) = Result.success(Unit)
        override suspend fun signInWithGoogle() = Result.success(Unit)
        override suspend fun signOut(): Result<Unit> { authState.value = AuthState.Unauthenticated; return Result.success(Unit) }
    }
    private class Repository : EventPreferencesRepository {
        var value = EventPreferences(languages = listOf("ja"), metrics = true)
        var fail = false
        override suspend fun load(): EventPreferences { check(!fail); return value }
        override suspend fun save(preferences: EventPreferences) { check(!fail); value = preferences }
        override suspend fun withdraw() { check(!fail); value = EventPreferences() }
    }

    @Test fun failedWithdrawalIsNotReportedAsSuccessAndCanRetry() = runTest(dispatcher) {
        val repository = Repository()
        val vm = EventPreferencesViewModel(repository, Auth())
        advanceUntilIdle()
        assertTrue(vm.state.value.preferences.metrics)
        repository.fail = true
        vm.withdraw()
        advanceUntilIdle()
        assertEquals(PreferenceMessage.Failed, vm.state.value.message)
        assertTrue(vm.state.value.preferences.metrics)
        repository.fail = false
        vm.withdraw()
        advanceUntilIdle()
        assertEquals(PreferenceMessage.Withdrawn, vm.state.value.message)
        assertEquals(EventPreferences(), vm.state.value.preferences)
    }

    @Test fun missingBackendDoesNotPretendToSaveAndLogoutClearsTags() = runTest(dispatcher) {
        val repository = Repository().apply { fail = true }
        val auth = Auth()
        val vm = EventPreferencesViewModel(repository, auth)
        advanceUntilIdle()
        assertFalse(vm.state.value.loaded)
        vm.save()
        assertNotEquals(PreferenceMessage.Saved, vm.state.value.message)
        repository.fail = false
        vm.reload()
        advanceUntilIdle()
        assertEquals(listOf("ja"), vm.state.value.preferences.languages)
        auth.signOut()
        advanceUntilIdle()
        assertFalse(vm.state.value.signedIn)
        assertEquals(EventPreferences(), vm.state.value.preferences)
    }
}
