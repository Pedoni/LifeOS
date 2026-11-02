package it.edon.lifeos.feature.auth.signin

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.auth.EmailIssue
import it.edon.lifeos.core.domain.auth.PasswordIssue
import it.edon.lifeos.core.domain.auth.SignInUseCase
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.feature.auth.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignInViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<AuthRepository>()
    private val viewModel = SignInViewModel(SignInUseCase(repository))
    private val user = AuthUser("u1", "ada@example.com", null)

    private fun fill(email: String = "ada@example.com", password: String = "secret-password") {
        viewModel.onEvent(SignInEvent.EmailChanged(email))
        viewModel.onEvent(SignInEvent.PasswordChanged(password))
    }

    @Test
    fun startsEmptyAndIdle() {
        assertEquals(SignInUiState(), viewModel.uiState.value)
    }

    @Test
    fun typingUpdatesTheFields() {
        fill(email = "ada", password = "pw")

        assertEquals("ada", viewModel.uiState.value.email)
        assertEquals("pw", viewModel.uiState.value.password)
    }

    @Test
    fun thePasswordCanBeShownAndHidden() {
        viewModel.onEvent(SignInEvent.TogglePasswordVisibility)
        assertTrue(viewModel.uiState.value.isPasswordVisible)

        viewModel.onEvent(SignInEvent.TogglePasswordVisibility)
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun invalidInputIsReportedPerFieldAndNothingIsSent() {
        fill(email = "not-an-email", password = "")

        viewModel.onEvent(SignInEvent.Submit)

        val state = viewModel.uiState.value
        assertEquals(EmailIssue.INVALID, state.emailIssue)
        assertEquals(PasswordIssue.BLANK, state.passwordIssue)
        assertFalse(state.isSubmitting)
        coVerify(exactly = 0) { repository.signIn(any(), any()) }
    }

    @Test
    fun aSuccessfulSignInEndsWithoutAnError() {
        coEvery { repository.signIn("ada@example.com", "secret-password") } returns
            Outcome.Success(user)
        fill(email = "  ada@example.com ")

        viewModel.onEvent(SignInEvent.Submit)

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertNull(state.error)
        coVerify(exactly = 1) { repository.signIn("ada@example.com", "secret-password") }
    }

    @Test
    fun aRejectedSignInShowsTheError() {
        coEvery { repository.signIn(any(), any()) } returns
            Outcome.Failure(AuthError.InvalidCredentials)
        fill()

        viewModel.onEvent(SignInEvent.Submit)

        val state = viewModel.uiState.value
        assertEquals(AuthError.InvalidCredentials, state.error)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun editingAFieldClearsTheError() {
        coEvery { repository.signIn(any(), any()) } returns Outcome.Failure(AuthError.Network)
        fill()
        viewModel.onEvent(SignInEvent.Submit)

        viewModel.onEvent(SignInEvent.PasswordChanged("another-password"))

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun theFormIsBusyWhileWaitingForTheServer() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { repository.signIn(any(), any()) } coAnswers {
            gate.await()
            Outcome.Success(user)
        }
        fill()

        viewModel.uiState.test {
            assertFalse(awaitItem().isSubmitting)

            viewModel.onEvent(SignInEvent.Submit)
            assertTrue(awaitItem().isSubmitting)

            gate.complete(Unit)
            assertFalse(awaitItem().isSubmitting)
        }
    }

    @Test
    fun aSecondSubmitWhileBusyIsIgnored() {
        val gate = CompletableDeferred<Unit>()
        coEvery { repository.signIn(any(), any()) } coAnswers {
            gate.await()
            Outcome.Success(user)
        }
        fill()

        viewModel.onEvent(SignInEvent.Submit)
        viewModel.onEvent(SignInEvent.Submit)
        gate.complete(Unit)

        coVerify(exactly = 1) { repository.signIn(any(), any()) }
    }
}
