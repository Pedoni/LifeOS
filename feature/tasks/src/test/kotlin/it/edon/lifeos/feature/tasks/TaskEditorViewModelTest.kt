package it.edon.lifeos.feature.tasks

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.mockk
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.domain.task.DurationIssue
import it.edon.lifeos.core.domain.task.SaveTaskUseCase
import it.edon.lifeos.core.domain.task.TitleIssue
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TaskEditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(Instant.parse("2026-03-10T09:00:00Z"), ZoneOffset.UTC)
    private val authState =
        MutableStateFlow<AuthState>(AuthState.SignedIn(AuthUser("u1", "a@b.c", null)))
    private val authRepository = mockk<AuthRepository>().also {
        every { it.authState } returns
            authState
    }
    private val repository = FakeTaskRepository()

    private fun viewModel(taskId: String? = null) = TaskEditorViewModel(
        SavedStateHandle(mapOfNotNull("taskId" to taskId)),
        authRepository,
        repository,
        SaveTaskUseCase(repository, clock)
    )

    private fun mapOfNotNull(vararg pairs: Pair<String, String?>) =
        pairs.filter { it.second != null }.associate { it.first to it.second }

    private fun TaskEditorViewModel.form() = uiState.value as TaskEditorUiState.Form

    @Test
    fun aNewTaskStartsAsAnEmptyForm() {
        val form = viewModel().form()

        assertTrue(form.isNew)
        assertEquals("", form.title)
        assertEquals(TaskPriority.MEDIUM, form.priority)
        assertEquals(TaskStatus.TODO, form.status)
    }

    @Test
    fun anExistingTaskIsLoadedIntoTheForm() {
        repository.tasks.value = mapOf("t1" to testTask("t1", title = "Existing"))

        val form = viewModel("t1").form()

        assertFalse(form.isNew)
        assertEquals("Existing", form.title)
        assertEquals("details", form.description)
        assertEquals("30", form.duration)
    }

    @Test
    fun aMissingTaskIsReportedAsNotFound() {
        assertEquals(TaskEditorUiState.NotFound, viewModel("gone").uiState.value)
    }

    @Test
    fun savingWithoutATitleShowsTheIssueAndKeepsTheForm() = runTest {
        val viewModel = viewModel()
        viewModel.onEvent(TaskEditorEvent.DurationChanged("0"))

        viewModel.onEvent(TaskEditorEvent.Save)

        val form = viewModel.form()
        assertEquals(TitleIssue.BLANK, form.titleIssue)
        assertEquals(DurationIssue.NOT_POSITIVE, form.durationIssue)
        assertFalse(form.isSaving)
        assertFalse(form.isFinished)
        assertTrue(repository.tasks.value.isEmpty())
    }

    @Test
    fun typingClearsTheIssueOfThatField() {
        val viewModel = viewModel()
        viewModel.onEvent(TaskEditorEvent.Save)

        viewModel.onEvent(TaskEditorEvent.TitleChanged("W"))

        assertNull(viewModel.form().titleIssue)
    }

    @Test
    fun savingAValidFormStoresTheTaskForTheSignedInUserAndFinishes() = runTest {
        val viewModel = viewModel()
        viewModel.onEvent(TaskEditorEvent.TitleChanged("Write report"))
        viewModel.onEvent(TaskEditorEvent.PrioritySelected(TaskPriority.URGENT))
        viewModel.onEvent(TaskEditorEvent.DateSelected(LocalDate.parse("2026-03-12")))

        viewModel.onEvent(TaskEditorEvent.Save)

        val saved = repository.tasks.value.values.single()
        assertEquals("u1", saved.userId)
        assertEquals(TaskPriority.URGENT, saved.priority)
        assertEquals(DueDate.AllDay(LocalDate.parse("2026-03-12")), saved.due)
        assertTrue(viewModel.form().isFinished)
    }

    @Test
    fun aDueDateWithATimeIsAnExactMomentInTheLocalZone() = runTest {
        val viewModel = viewModel()
        viewModel.onEvent(TaskEditorEvent.TitleChanged("Call"))
        viewModel.onEvent(TaskEditorEvent.DateSelected(LocalDate.parse("2026-03-12")))
        viewModel.onEvent(TaskEditorEvent.TimeSelected(LocalTime.of(17, 30)))

        viewModel.onEvent(TaskEditorEvent.Save)

        val expected = LocalDate.parse(
            "2026-03-12"
        ).atTime(17, 30).atZone(ZoneId.systemDefault()).toInstant()
        assertEquals(DueDate.At(expected), repository.tasks.value.values.single().due)
    }

    @Test
    fun clearingTheDueDateAlsoClearsTheTime() {
        val viewModel = viewModel()
        viewModel.onEvent(TaskEditorEvent.DateSelected(LocalDate.parse("2026-03-12")))
        viewModel.onEvent(TaskEditorEvent.TimeSelected(LocalTime.NOON))

        viewModel.onEvent(TaskEditorEvent.ClearDue)

        assertNull(viewModel.form().dueDate)
        assertNull(viewModel.form().dueTime)
    }

    @Test
    fun editingSavesOverTheSameTask() = runTest {
        repository.tasks.value = mapOf("t1" to testTask("t1", title = "Old"))
        val viewModel = viewModel("t1")
        viewModel.onEvent(TaskEditorEvent.TitleChanged("New"))

        viewModel.onEvent(TaskEditorEvent.Save)

        assertEquals(setOf("t1"), repository.tasks.value.keys)
        assertEquals("New", repository.tasks.value.getValue("t1").title)
    }

    @Test
    fun aTaskDeletedWhileEditingBecomesNotFoundOnSave() = runTest {
        repository.tasks.value = mapOf("t1" to testTask("t1"))
        val viewModel = viewModel("t1")
        repository.delete("t1")

        viewModel.onEvent(TaskEditorEvent.Save)

        assertEquals(TaskEditorUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun deletingNeedsConfirmationAndThenRemovesTheTask() = runTest {
        repository.tasks.value = mapOf("t1" to testTask("t1"))
        val viewModel = viewModel("t1")

        viewModel.onEvent(TaskEditorEvent.RequestDelete)
        assertTrue(viewModel.form().isDeleteDialogVisible)
        assertEquals(1, repository.tasks.value.size)

        viewModel.onEvent(TaskEditorEvent.ConfirmDelete)

        assertTrue(repository.tasks.value.isEmpty())
        assertTrue(viewModel.form().isFinished)
    }

    @Test
    fun dismissingTheDeleteDialogKeepsTheTask() {
        repository.tasks.value = mapOf("t1" to testTask("t1"))
        val viewModel = viewModel("t1")
        viewModel.onEvent(TaskEditorEvent.RequestDelete)

        viewModel.onEvent(TaskEditorEvent.DismissDelete)

        assertFalse(viewModel.form().isDeleteDialogVisible)
        assertEquals(1, repository.tasks.value.size)
    }
}
