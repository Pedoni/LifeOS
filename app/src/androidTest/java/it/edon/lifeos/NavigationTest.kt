package it.edon.lifeos

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

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
}
