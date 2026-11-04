package it.edon.lifeos.feature.auth.signup

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.auth.DisplayNameIssue
import it.edon.lifeos.core.domain.auth.EmailIssue
import it.edon.lifeos.core.domain.auth.PasswordIssue
import it.edon.lifeos.core.domain.auth.SignUpUseCase
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.SignUpResult
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.feature.auth.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SignUpViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<AuthRepository>()
    private val viewModel = SignUpViewModel(SignUpUseCase(repository))

    private fun fill(
        name: String = "Ada",
        email: String = "ada@example.com",
        password: String = "secret-password"
    ) {
        viewModel.onEvent(SignUpEvent.DisplayNameChanged(name))
        viewModel.onEvent(SignUpEvent.EmailChanged(email))
        viewModel.onEvent(SignUpEvent.PasswordChanged(password))
    }

    @Test
    fun startsEmptyAndIdle() {
        assertEquals(SignUpUiState(), viewModel.uiState.value)
    }

    @Test
    fun everyInvalidFieldIsReportedAtOnceAndNothingIsSent() {
        fill(name = "a".repeat(101), email = "nope", password = "short")

        viewModel.onEvent(SignUpEvent.Submit)

        val state = viewModel.uiState.value
        assertEquals(DisplayNameIssue.TOO_LONG, state.displayNameIssue)
        assertEquals(EmailIssue.INVALID, state.emailIssue)
        assertEquals(PasswordIssue.TOO_SHORT, state.passwordIssue)
        coVerify(exactly = 0) { repository.signUp(any(), any(), any()) }
    }

    @Test
    fun aConfirmationEmailLeadsToTheCheckYourEmailState() {
        coEvery { repository.signUp(any(), any(), any()) } returns
            Outcome.Success(SignUpResult(requiresEmailConfirmation = true))
        fill(email = "  ada@example.com ")

        viewModel.onEvent(SignUpEvent.Submit)

        val state = viewModel.uiState.value
        assertEquals("ada@example.com", state.confirmationSentTo)
        assertFalse(state.isSubmitting)
        assertNull(state.error)
    }

    @Test
    fun noConfirmationNeededLeavesTheStateReadyForTheSwitchToTheApp() {
        coEvery { repository.signUp(any(), any(), any()) } returns
            Outcome.Success(SignUpResult(requiresEmailConfirmation = false))
        fill()

        viewModel.onEvent(SignUpEvent.Submit)

        assertNull(viewModel.uiState.value.confirmationSentTo)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun theDisplayNameIsSentWhenGivenAndAbsentWhenBlank() {
        coEvery { repository.signUp(any(), any(), any()) } returns
            Outcome.Success(SignUpResult(false))

        fill(name = "  Ada  ")
        viewModel.onEvent(SignUpEvent.Submit)
        fill(name = "   ")
        viewModel.onEvent(SignUpEvent.Submit)

        coVerify(exactly = 1) { repository.signUp("ada@example.com", "secret-password", "Ada") }
        coVerify(exactly = 1) { repository.signUp("ada@example.com", "secret-password", null) }
    }

    @Test
    fun anAlreadyRegisteredEmailShowsTheError() {
        coEvery { repository.signUp(any(), any(), any()) } returns
            Outcome.Failure(AuthError.EmailAlreadyRegistered)
        fill()

        viewModel.onEvent(SignUpEvent.Submit)

        assertEquals(AuthError.EmailAlreadyRegistered, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun editingAFieldClearsItsIssueAndTheError() {
        fill(email = "nope")
        viewModel.onEvent(SignUpEvent.Submit)

        viewModel.onEvent(SignUpEvent.EmailChanged("ada@example.com"))

        assertNull(viewModel.uiState.value.emailIssue)
        assertNull(viewModel.uiState.value.error)
    }
}
