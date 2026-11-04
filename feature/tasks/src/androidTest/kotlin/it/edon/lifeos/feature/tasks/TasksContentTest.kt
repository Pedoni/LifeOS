package it.edon.lifeos.feature.tasks

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.model.CategoryType
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import it.edon.lifeos.core.domain.task.SortDirection
import it.edon.lifeos.core.domain.task.TaskFilter
import it.edon.lifeos.core.domain.task.TaskListQuery
import it.edon.lifeos.core.domain.task.TaskView
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

    private fun content(
        vararg rows: TaskRow,
        query: TaskListQuery = TaskListQuery(),
        categories: List<Category> = emptyList(),
        options: Boolean = false,
        grouped: Boolean = false
    ) = TasksUiState.Content(
        query = query,
        groups = if (grouped) {
            rows.groupBy {
                it.task.priority
            }.map { (priority, list) -> TaskRowGroup(priority, list) }
        } else {
            listOf(TaskRowGroup(null, rows.toList())).filter { it.rows.isNotEmpty() }
        },
        categories = categories,
        isOptionsVisible = options
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
        show(content(TaskRow(task("a", "Write report"), isOverdue = true)))

        composeRule.onNodeWithText("Write report").assertExists()
        composeRule.onNodeWithText("Urgent", substring = true).assertExists()
        // The view chip and the marker on the task.
        composeRule.onAllNodesWithText("Overdue").assertCountEquals(2)
    }

    @Test
    fun tappingTheCheckboxCompletesTheTask() {
        show(content(TaskRow(task("a", "Write report"), isOverdue = false)))

        composeRule.onNodeWithContentDescription("Mark Write report as done").performClick()

        assertEquals(listOf<TasksEvent>(TasksEvent.SetCompleted("a", true)), events)
    }

    @Test
    fun aCompletedTaskOffersToBeReopened() {
        show(content(TaskRow(task("a", "Write report", TaskStatus.COMPLETED), isOverdue = false)))

        composeRule.onNodeWithContentDescription("Mark Write report as not done").performClick()

        assertEquals(listOf<TasksEvent>(TasksEvent.SetCompleted("a", false)), events)
    }

    @Test
    fun tappingTheTitleOpensTheTask() {
        show(content(TaskRow(task("a", "Write report"), isOverdue = false)))

        composeRule.onNodeWithText("Write report").performClick()

        assertEquals(listOf("a"), opened)
    }

    @Test
    fun contentOffersToAddAnotherTask() {
        show(content(TaskRow(task("a", "Write report"), isOverdue = false)))

        // A screen reader announces the description of the button itself.
        composeRule.onNodeWithContentDescription("New task").performClick()

        assertEquals(1, added)
    }

    @Test
    fun choosingAViewIsReported() {
        show(content(TaskRow(task("a", "Write report"), isOverdue = false)))

        composeRule.onNodeWithText("Overdue").performClick()

        assertEquals(listOf<TasksEvent>(TasksEvent.ViewSelected(TaskView.OVERDUE)), events)
    }

    @Test
    fun aViewThatMatchesNothingSaysSoAndOffersAReset() {
        show(content(query = TaskListQuery(view = TaskView.OVERDUE)))

        composeRule.onNodeWithText("Nothing here").assertExists()
        composeRule.onNodeWithText("Reset view and filters").performClick()

        assertEquals(listOf<TasksEvent>(TasksEvent.ResetQuery), events)
    }

    @Test
    fun theAllViewWithNothingToShowDoesNotOfferAReset() {
        show(content())

        composeRule.onNodeWithText("Nothing here").assertExists()
        composeRule.onNodeWithText("Reset view and filters").assertDoesNotExist()
    }

    @Test
    fun groupsAreTitledByPriority() {
        show(
            content(
                TaskRow(task("a", "Write report"), isOverdue = false),
                query = TaskListQuery(groupByPriority = true),
                grouped = true
            )
        )

        composeRule.onNodeWithText("Urgent").assertExists()
    }

    @Test
    fun theCategoryOfATaskIsWrittenOut() {
        val work =
            Category("c1", "u1", "Work", "#3F51B5", CategoryType.TASK, Instant.EPOCH, Instant.EPOCH)
        show(content(TaskRow(task("a", "Write report"), isOverdue = false, category = work)))

        composeRule.onNodeWithText("Work").assertExists()
    }

    @Test
    fun theOptionsButtonOpensTheSheetAndSaysWhenFiltersAreOn() {
        show(content(TaskRow(task("a", "x"), false), query = TaskListQuery(groupByPriority = true)))

        composeRule.onNodeWithText("Filter and sort (on)").performClick()

        assertEquals(listOf<TasksEvent>(TasksEvent.ShowOptions), events)
    }

    @Test
    fun theSheetOffersSortingGroupingAndFilters() {
        val work =
            Category("c1", "u1", "Work", "#3F51B5", CategoryType.TASK, Instant.EPOCH, Instant.EPOCH)
        show(
            content(
                TaskRow(task("a", "x"), false),
                categories = listOf(work),
                query = TaskListQuery(filter = TaskFilter(categoryId = "c1")),
                options = true
            )
        )

        composeRule.onNodeWithText("Sort by").assertExists()
        composeRule.onNodeWithText("Group by priority").assertExists()
        composeRule.onNodeWithText("Show only these priorities").assertExists()
        composeRule.onNodeWithText("Descending").performClick()
        assertEquals(
            listOf<TasksEvent>(TasksEvent.SortDirectionSelected(SortDirection.DESCENDING)),
            events
        )
    }

    @Test
    fun theSheetHidesTheCategoryFilterWhenThereAreNoCategories() {
        show(content(TaskRow(task("a", "x"), false), options = true))

        composeRule.onNodeWithText("Show only these priorities").assertExists()
        composeRule.onNodeWithText("Any").assertDoesNotExist()
    }
}
