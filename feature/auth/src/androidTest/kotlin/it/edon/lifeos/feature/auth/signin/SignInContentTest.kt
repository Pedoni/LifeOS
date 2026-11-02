package it.edon.lifeos.feature.auth.signin

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.auth.EmailIssue
import it.edon.lifeos.core.domain.model.AuthError
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SignInContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val events = mutableListOf<SignInEvent>()
    private var createAccountClicks = 0

    private fun show(state: SignInUiState) {
        composeRule.setContent {
            LifeOSTheme {
                SignInContent(
                    state = state,
                    onEvent = { events += it },
                    onCreateAccount = { createAccountClicks++ }
                )
            }
        }
    }

    @Test
    fun pressingTheButtonSubmits() {
        show(SignInUiState(email = "ada@example.com", password = "secret-password"))

        composeRule.onNodeWithText("Sign in").performClick()

        assertEquals(listOf<SignInEvent>(SignInEvent.Submit), events)
    }

    @Test
    fun aFailedAttemptShowsItsMessage() {
        show(SignInUiState(error = AuthError.InvalidCredentials))

        composeRule.onNodeWithText("The email or the password is not correct.").assertExists()
    }

    @Test
    fun aFieldProblemShowsItsMessage() {
        show(SignInUiState(emailIssue = EmailIssue.INVALID))

        composeRule.onNodeWithText("Enter a valid email address.").assertExists()
    }

    @Test
    fun whileSubmittingTheButtonCannotBePressed() {
        show(SignInUiState(isSubmitting = true))

        composeRule.onNodeWithContentDescription("Sign in").assertIsNotEnabled()
    }

    @Test
    fun theLinkToSignUpIsAvailableWhenIdle() {
        show(SignInUiState())

        composeRule.onNodeWithText("Create an account").assertIsEnabled().performClick()

        assertEquals(1, createAccountClicks)
    }
}
