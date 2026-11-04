package it.edon.lifeos.navigation

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import it.edon.lifeos.MainDispatcherRule
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SessionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<AuthRepository>()

    @Test
    fun isLoadingUntilTheStoredSessionIsKnown() {
        every { repository.authState } returns MutableSharedFlow()

        assertEquals(AuthState.Loading, SessionViewModel(repository).authState.value)
    }

    @Test
    fun followsTheAuthenticationState() = runTest {
        val states = MutableStateFlow<AuthState>(AuthState.SignedOut)
        every { repository.authState } returns states

        SessionViewModel(repository).authState.test {
            var state = awaitItem()
            if (state == AuthState.Loading) state = awaitItem()
            assertEquals(AuthState.SignedOut, state)

            val user = AuthUser("u1", "ada@example.com", null)
            states.value = AuthState.SignedIn(user)
            assertEquals(AuthState.SignedIn(user), awaitItem())
        }
    }
}
