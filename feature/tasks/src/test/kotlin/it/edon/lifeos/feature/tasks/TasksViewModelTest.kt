package it.edon.lifeos.feature.tasks

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskStatus
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.domain.repository.TaskRepository
import it.edon.lifeos.core.domain.task.SetTaskCompletedUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TasksViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = Instant.parse("2026-03-10T09:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val authState =
        MutableStateFlow<AuthState>(AuthState.SignedIn(AuthUser("u1", "a@b.c", null)))
    private val authRepository = mockk<AuthRepository>().also {
        every { it.authState } returns
            authState
    }

    private fun viewModel(repository: TaskRepository) = TasksViewModel(
        authRepository,
        repository,
        SetTaskCompletedUseCase(repository, clock),
        clock
    )

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
        val overdue = testTask("a", due = DueDate.AllDay(LocalDate.parse("2026-03-09")))
        val upcoming = testTask("b", due = DueDate.AllDay(LocalDate.parse("2026-03-11")))
        val doneAndLate = testTask(
            "c",
            status = TaskStatus.COMPLETED,
            completedAt = now,
            due = DueDate.AllDay(LocalDate.parse("2026-03-01"))
        )

        viewModel(FakeTaskRepository(overdue, upcoming, doneAndLate)).uiState.test {
            val content = awaitItem() as TasksUiState.Content
            assertEquals(
                mapOf("a" to true, "b" to false, "c" to false),
                content.rows.associate { it.task.id to it.isOverdue }
            )
        }
    }

    @Test
    fun completingATaskUpdatesTheList() = runTest {
        val repository = FakeTaskRepository(testTask("a"))
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            assertEquals(
                TaskStatus.TODO,
                (awaitItem() as TasksUiState.Content).rows.single().task.status
            )

            viewModel.onEvent(TasksEvent.SetCompleted("a", completed = true))

            val content = awaitItem() as TasksUiState.Content
            assertEquals(TaskStatus.COMPLETED, content.rows.single().task.status)
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
