package it.edon.lifeos.feature.profile

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.model.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val events = mutableListOf<ProfileEvent>()

    private fun ready(
        canSave: Boolean = false,
        error: AuthError? = null,
        deleteDialog: Boolean = false
    ) = ProfileUiState.Ready(
        user = AuthUser("u1", "ada@example.com", "Ada"),
        preferences = UserPreferences(),
        displayName = "Ada",
        canSaveDisplayName = canSave,
        error = error,
        isDeleteDialogVisible = deleteDialog
    )

    private fun show(state: ProfileUiState) {
        composeRule.setContent {
            LifeOSTheme {
                ProfileContent(state = state, onEvent = { events += it }, onBack = {})
            }
        }
    }

    @Test
    fun showsTheEmailOfTheAccount() {
        show(ready())

        composeRule.onNodeWithText("ada@example.com").assertExists()
    }

    @Test
    fun savingTheNameIsOfferedOnlyWhenItChanged() {
        show(ready(canSave = false))
        composeRule.onNodeWithText("Save name").assertIsNotEnabled()
    }

    @Test
    fun savingTheNameSendsTheEvent() {
        show(ready(canSave = true))

        composeRule.onNodeWithText("Save name").assertIsEnabled().performClick()

        assertEquals(listOf<ProfileEvent>(ProfileEvent.SaveDisplayName), events)
    }

    @Test
    fun deletingTheAccountAsksFirst() {
        show(ready())

        composeRule.onNodeWithText("Delete account").performClick()

        assertEquals(listOf<ProfileEvent>(ProfileEvent.RequestAccountDeletion), events)
    }

    @Test
    fun theDeleteDialogCanBeConfirmedOrCancelled() {
        show(ready(deleteDialog = true))

        composeRule.onNodeWithText("Delete your account?").assertExists()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Delete").performClick()

        assertEquals(
            listOf(ProfileEvent.DismissAccountDeletion, ProfileEvent.ConfirmAccountDeletion),
            events
        )
    }

    @Test
    fun anErrorIsShown() {
        show(ready(error = AuthError.Network))

        composeRule.onNodeWithText(
            "Could not reach the server. Check your connection and try again."
        ).assertExists()
    }
}
