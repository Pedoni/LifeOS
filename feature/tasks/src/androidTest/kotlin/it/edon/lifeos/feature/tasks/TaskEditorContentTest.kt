package it.edon.lifeos.feature.tasks

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.task.TitleIssue
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TaskEditorContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val events = mutableListOf<TaskEditorEvent>()

    private fun show(state: TaskEditorUiState) {
        composeRule.setContent {
            LifeOSTheme {
                TaskEditorContent(state = state, onEvent = { events += it }, onBack = {})
            }
        }
    }

    @Test
    fun aNewTaskHasNoDeleteButton() {
        show(TaskEditorUiState.Form(isNew = true))

        composeRule.onNodeWithText("New task").assertExists()
        composeRule.onNodeWithText("Delete task").assertDoesNotExist()
    }

    @Test
    fun anExistingTaskCanBeDeletedAfterConfirming() {
        show(TaskEditorUiState.Form(isNew = false, title = "Write report"))

        composeRule.onNodeWithText("Delete task").performScrollTo().performClick()

        assertEquals(listOf<TaskEditorEvent>(TaskEditorEvent.RequestDelete), events)
    }

    @Test
    fun theDeleteDialogAsksBeforeDeleting() {
        show(TaskEditorUiState.Form(isNew = false, title = "x", isDeleteDialogVisible = true))

        composeRule.onNodeWithText("Delete this task?").assertExists()
        composeRule.onNodeWithText("Delete").performClick()

        assertEquals(listOf<TaskEditorEvent>(TaskEditorEvent.ConfirmDelete), events)
    }

    @Test
    fun typingReportsTheTitle() {
        show(TaskEditorUiState.Form(isNew = true))

        composeRule.onNodeWithText("Title").performTextInput("Call")

        assertEquals(listOf<TaskEditorEvent>(TaskEditorEvent.TitleChanged("Call")), events)
    }

    @Test
    fun aTitleProblemIsExplained() {
        show(TaskEditorUiState.Form(isNew = true, titleIssue = TitleIssue.BLANK))

        composeRule.onNodeWithText("Give the task a title.").assertExists()
    }

    @Test
    fun choosingAPriorityIsReported() {
        show(TaskEditorUiState.Form(isNew = true))

        composeRule.onNodeWithText("Urgent").performScrollTo().performClick()

        assertEquals(
            listOf<TaskEditorEvent>(TaskEditorEvent.PrioritySelected(TaskPriority.URGENT)),
            events
        )
    }

    @Test
    fun theTimeOptionAppearsOnlyOnceThereIsADate() {
        show(TaskEditorUiState.Form(isNew = true))
        composeRule.onNodeWithText("Set a time").assertDoesNotExist()
    }

    @Test
    fun withADateTheTimeAndRemoveOptionsAreOffered() {
        show(TaskEditorUiState.Form(isNew = true, dueDate = LocalDate.of(2026, 3, 12)))

        composeRule.onNodeWithText("Set a time").performScrollTo().assertExists()
        composeRule.onNodeWithText("Remove").performScrollTo().performClick()

        assertEquals(listOf<TaskEditorEvent>(TaskEditorEvent.ClearDue), events)
    }

    @Test
    fun savingDisablesTheFormActions() {
        show(TaskEditorUiState.Form(isNew = false, title = "x", isSaving = true))

        composeRule.onNodeWithText("Delete task").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun aVanishedTaskExplainsAndOffersToGoBack() {
        show(TaskEditorUiState.NotFound)

        composeRule.onNodeWithText("This task no longer exists").assertExists()
        composeRule.onNodeWithText("Back to tasks").assertExists()
    }

    @Test
    fun loadingShowsAMessage() {
        show(TaskEditorUiState.Loading)

        composeRule.onNodeWithText("Loading the task").assertExists()
    }
}
