package it.edon.lifeos

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.navigation.MainContent
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests the signed-in part of the app on its own: who is signed in is decided above it, by a
 * state that needs a real account, so it is not part of these tests.
 */
class NavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var signOutClicks = 0

    @Before
    fun showMainContent() {
        composeRule.setContent {
            LifeOSTheme {
                MainContent(onSignOut = { signOutClicks++ })
            }
        }
    }

    // A destination's title and its tab share the same text, so tabs are matched by role.
    private fun tab(label: String) = hasText(label) and
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    @Test
    fun startsOnHome() {
        composeRule.onNode(tab("Home")).assertIsSelected()
    }

    @Test
    fun tappingATabSelectsItAndShowsItsScreen() {
        composeRule.onNode(tab("Tasks")).performClick()

        composeRule.onNode(tab("Tasks")).assertIsSelected()
        composeRule.onNodeWithText("This section is not available yet.").assertExists()
    }

    @Test
    fun everyTabIsReachable() {
        listOf("Home", "Tasks", "Calendar", "Habits", "More").forEach { label ->
            composeRule.onNode(tab(label)).performClick()
            composeRule.onNode(tab(label)).assertIsSelected()
        }
    }

    @Test
    fun moreOffersToSignOut() {
        composeRule.onNode(tab("More")).performClick()

        composeRule.onNodeWithText("Sign out").performClick()

        assertEquals(1, signOutClicks)
    }
}
