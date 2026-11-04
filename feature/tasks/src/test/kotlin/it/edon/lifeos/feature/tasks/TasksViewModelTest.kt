package it.edon.lifeos.feature.tasks

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.model.CategoryType
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.domain.repository.TaskRepository
import it.edon.lifeos.core.domain.task.SetTaskCompletedUseCase
import it.edon.lifeos.core.domain.task.SortDirection
import it.edon.lifeos.core.domain.task.TaskListQuery
import it.edon.lifeos.core.domain.task.TaskSortField
import it.edon.lifeos.core.domain.task.TaskView
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TasksViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Noon, so that "today" is the same calendar day in every time zone the tests may run in.
    private val now = Instant.parse("2026-03-10T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val authState =
        MutableStateFlow<AuthState>(AuthState.SignedIn(AuthUser("u1", "a@b.c", null)))
    private val authRepository =
        mockk<AuthRepository>().also { every { it.authState } returns authState }
    private val categories = FakeCategoryRepository()

    private fun viewModel(repository: TaskRepository) = TasksViewModel(
        authRepository,
        repository,
        categories,
        SetTaskCompletedUseCase(repository, clock),
        clock
    )

    private fun TasksUiState.content() = this as TasksUiState.Content

    private fun TasksUiState.ids() = content().groups.flatMap { group ->
        group.rows.map { it.task.id }
    }

    // A due day relative to the day the test runs on, whatever the time zone of the machine.
    private fun dayFromToday(days: Long) =
        DueDate.AllDay(LocalDate.now(clock.withZone(ZoneId.systemDefault())).plusDays(days))

    @Test
    fun showsLoadingUntilSignedIn() = runTest {
        authState.value = AuthState.Loading

        viewModel(FakeTaskRepository()).uiState.test {
            assertEquals(TasksUiState.Loading, awaitItem())
        }
    }

    @Test
    fun noTasksIsTheEmptyState() = runTest {
        viewModel(FakeTaskRepository()).uiState.test {
            assertEquals(TasksUiState.Empty, awaitItem())
        }
    }

    @Test
    fun tasksAreShownAndOnlyOpenPastDueOnesAreOverdue() = runTest {
        val overdue = testTask("a", due = dayFromToday(-2))
        val upcoming = testTask("b", due = dayFromToday(2))
        val doneAndLate = testTask(
            "c",
            status = TaskStatus.COMPLETED,
            completedAt = now,
            due = dayFromToday(-5)
        )

        viewModel(FakeTaskRepository(overdue, upcoming, doneAndLate)).uiState.test {
            val rows = awaitItem().content().groups.single().rows
            assertEquals(
                mapOf("a" to true, "b" to false, "c" to false),
                rows.associate { it.task.id to it.isOverdue }
            )
        }
    }

    @Test
    fun theCategoryOfATaskIsShownWithIt() = runTest {
        categories.save(testCategory("work", "Work"))
        categories.save(testCategory("goal", "Reading", CategoryType.GOAL))
        val repository = FakeTaskRepository(testTask("a", categoryId = "work"), testTask("b"))

        viewModel(repository).uiState.test {
            val content = awaitItem().content()
            assertEquals(
                "Work",
                content.groups.single().rows.first {
                    it.task.id == "a"
                }.category?.name
            )
            assertEquals(listOf("Work"), content.categories.map { it.name })
        }
    }

    @Test
    fun choosingAViewNarrowsTheList() = runTest {
        val repository = FakeTaskRepository(
            testTask("late", due = dayFromToday(-1)),
            testTask("soon", due = dayFromToday(1)),
            testTask("done", status = TaskStatus.COMPLETED, completedAt = now)
        )
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            assertEquals(3, awaitItem().ids().size)

            viewModel.onEvent(TasksEvent.ViewSelected(TaskView.OVERDUE))
            assertEquals(listOf("late"), awaitItem().ids())

            viewModel.onEvent(TasksEvent.ViewSelected(TaskView.COMPLETED))
            assertEquals(listOf("done"), awaitItem().ids())
        }
    }

    @Test
    fun anEmptyViewKeepsTheContentStateSoThePersonCanChangeIt() = runTest {
        val viewModel = viewModel(FakeTaskRepository(testTask("a", due = dayFromToday(3))))

        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(TasksEvent.ViewSelected(TaskView.OVERDUE))

            val content = awaitItem().content()
            assertTrue(content.groups.isEmpty())
            assertEquals(TaskView.OVERDUE, content.query.view)
        }
    }

    @Test
    fun sortingGroupingAndFiltersChangeTheQuery() = runTest {
        val repository = FakeTaskRepository(
            testTask("low", title = "b", priority = TaskPriority.LOW),
            testTask("urgent", title = "a", priority = TaskPriority.URGENT)
        )
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(TasksEvent.SortFieldSelected(TaskSortField.TITLE))
            viewModel.onEvent(TasksEvent.SortDirectionSelected(SortDirection.DESCENDING))
            assertEquals(listOf("low", "urgent"), expectMostRecentItem().ids())

            viewModel.onEvent(TasksEvent.GroupByPriorityChanged(true))
            assertEquals(
                listOf(TaskPriority.URGENT, TaskPriority.LOW),
                expectMostRecentItem().content().groups.map { it.priority }
            )

            viewModel.onEvent(TasksEvent.PriorityToggled(TaskPriority.LOW))
            assertEquals(listOf("low"), expectMostRecentItem().ids())

            viewModel.onEvent(TasksEvent.PriorityToggled(TaskPriority.LOW))
            assertEquals(2, expectMostRecentItem().ids().size)
        }
    }

    @Test
    fun filteringByCategoryAndResetting() = runTest {
        categories.save(testCategory("work", "Work"))
        val repository = FakeTaskRepository(
            testTask("a", categoryId = "work"),
            testTask("b")
        )
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(TasksEvent.CategoryFilterSelected("work"))
            assertEquals(listOf("a"), awaitItem().ids())

            viewModel.onEvent(TasksEvent.ResetQuery)
            val content = awaitItem().content()
            assertEquals(TaskListQuery(), content.query)
            assertEquals(2, content.groups.single().rows.size)
        }
    }

    @Test
    fun theOptionsSheetOpensAndCloses() = runTest {
        val viewModel = viewModel(FakeTaskRepository(testTask("a")))

        viewModel.uiState.test {
            assertFalse(awaitItem().content().isOptionsVisible)

            viewModel.onEvent(TasksEvent.ShowOptions)
            assertTrue(awaitItem().content().isOptionsVisible)

            viewModel.onEvent(TasksEvent.DismissOptions)
            assertFalse(awaitItem().content().isOptionsVisible)
        }
    }

    @Test
    fun completingATaskUpdatesTheList() = runTest {
        val repository = FakeTaskRepository(testTask("a"))
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            assertEquals(
                TaskStatus.TODO,
                awaitItem().content().groups.single().rows.single().task.status
            )

            viewModel.onEvent(TasksEvent.SetCompleted("a", completed = true))

            assertEquals(
                TaskStatus.COMPLETED,
                awaitItem().content().groups.single().rows.single().task.status
            )
        }
    }

    @Test
    fun aFailedReadShowsTheErrorAndRetryReadsAgain() = runTest {
        var attempts = 0
        val repository = object : TaskRepository by FakeTaskRepository(testTask("a")) {
            override fun observeTasks(userId: String): Flow<List<Task>> = flow {
                attempts++
                if (attempts == 1) error("database unavailable")
                emit(listOf(testTask("a")))
            }
        }
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            assertEquals(TasksUiState.Error, awaitItem())

            viewModel.onEvent(TasksEvent.Retry)

            assertTrue(awaitItem() is TasksUiState.Content)
        }
    }
}
