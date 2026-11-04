package it.edon.lifeos.feature.tasks

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TasksContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val events = mutableListOf<TasksEvent>()
    private var added = 0
    private val opened = mutableListOf<String>()

    private fun task(id: String, title: String, status: TaskStatus = TaskStatus.TODO) = Task(
        id = id,
        userId = "u1",
        title = title,
        description = null,
        notes = null,
        status = status,
        priority = TaskPriority.URGENT,
        due = DueDate.At(Instant.parse("2026-03-12T10:00:00Z")),
        estimatedDurationMinutes = null,
        categoryId = null,
        completedAt = if (status == TaskStatus.COMPLETED) Instant.EPOCH else null,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    private fun show(state: TasksUiState) {
        composeRule.setContent {
            LifeOSTheme {
                TasksContent(
                    state = state,
                    onEvent = { events += it },
                    onAddTask = { added++ },
                    onOpenTask = { opened += it }
                )
            }
        }
    }

    @Test
    fun loadingShowsAMessage() {
        show(TasksUiState.Loading)

        composeRule.onNodeWithText("Loading your tasks").assertExists()
    }

    @Test
    fun emptyExplainsWhatToDoAndOffersToAddATask() {
        show(TasksUiState.Empty)

        composeRule.onNodeWithText("No tasks yet").assertExists()
        composeRule.onNodeWithText("New task").performClick()
        assertEquals(1, added)
    }

    @Test
    fun errorOffersToTryAgain() {
        show(TasksUiState.Error)

        composeRule.onNodeWithText("Try again").performClick()

        assertEquals(listOf<TasksEvent>(TasksEvent.Retry), events)
    }

    @Test
    fun contentListsTheTasksWithPriorityWrittenOutAndOverdueMarked() {
        show(TasksUiState.Content(listOf(TaskRow(task("a", "Write report"), isOverdue = true))))

        composeRule.onNodeWithText("Write report").assertExists()
        composeRule.onNodeWithText("Urgent", substring = true).assertExists()
        composeRule.onNodeWithText("Overdue").assertExists()
    }

    @Test
    fun tappingTheCheckboxCompletesTheTask() {
        show(TasksUiState.Content(listOf(TaskRow(task("a", "Write report"), isOverdue = false))))

        composeRule.onNodeWithContentDescription("Mark Write report as done").performClick()

        assertEquals(listOf<TasksEvent>(TasksEvent.SetCompleted("a", true)), events)
    }

    @Test
    fun aCompletedTaskOffersToBeReopened() {
        show(
            TasksUiState.Content(
                listOf(TaskRow(task("a", "Write report", TaskStatus.COMPLETED), isOverdue = false))
            )
        )

        composeRule.onNodeWithContentDescription("Mark Write report as not done").performClick()

        assertEquals(listOf<TasksEvent>(TasksEvent.SetCompleted("a", false)), events)
    }

    @Test
    fun tappingTheTitleOpensTheTask() {
        show(TasksUiState.Content(listOf(TaskRow(task("a", "Write report"), isOverdue = false))))

        composeRule.onNodeWithText("Write report").performClick()

        assertEquals(listOf("a"), opened)
    }

    @Test
    fun contentOffersToAddAnotherTask() {
        show(TasksUiState.Content(listOf(TaskRow(task("a", "Write report"), isOverdue = false))))

        // A screen reader announces the description of the button itself.
        composeRule.onNodeWithContentDescription("New task").performClick()

        assertEquals(1, added)
    }
}
