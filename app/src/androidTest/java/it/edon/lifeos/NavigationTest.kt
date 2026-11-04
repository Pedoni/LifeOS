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

    @Before
    fun showMainContent() {
        composeRule.setContent {
            LifeOSTheme {
                MainContent()
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
        composeRule.onNode(tab("Calendar")).performClick()

        composeRule.onNode(tab("Calendar")).assertIsSelected()
        composeRule.onNodeWithText("This section is not available yet.").assertExists()
    }

    // Tasks is left out: its screen needs Hilt for its view model, like the profile.
    @Test
    fun everyPlaceholderTabIsReachable() {
        listOf("Home", "Calendar", "Habits", "More").forEach { label ->
            composeRule.onNode(tab(label)).performClick()
            composeRule.onNode(tab(label)).assertIsSelected()
        }
    }

    // Opening the profile needs Hilt for its view model, so only the entry is checked here.
    @Test
    fun moreOffersTheProfile() {
        composeRule.onNode(tab("More")).performClick()

        composeRule.onNodeWithText("Profile").assertExists()
    }
}
