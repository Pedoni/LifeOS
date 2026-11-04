package it.edon.lifeos.core.domain.task

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveTaskUseCaseTest {
    private val now = Instant.parse("2026-03-10T09:00:00Z")
    private val repository = FakeTaskRepository()
    private val useCase = SaveTaskUseCase(repository, Clock.fixed(now, ZoneOffset.UTC))

    private fun input(
        id: String? = null,
        title: String = "Write report",
        duration: String = "",
        status: TaskStatus = TaskStatus.TODO,
        due: DueDate? = null
    ) = TaskInput(
        id = id,
        title = title,
        description = "",
        notes = "",
        status = status,
        priority = TaskPriority.MEDIUM,
        due = due,
        estimatedDurationMinutes = duration
    )

    private fun stored(status: TaskStatus = TaskStatus.TODO, completedAt: Instant? = null) = Task(
        id = "t1",
        userId = "user",
        title = "Old",
        description = null,
        notes = null,
        status = status,
        priority = TaskPriority.LOW,
        due = null,
        estimatedDurationMinutes = null,
        categoryId = "cat",
        completedAt = completedAt,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-02T00:00:00Z")
    )

    @Test
    fun createsATaskWithAGeneratedIdAndTrimmedText() = runTest {
        val outcome = useCase(
            "user",
            input(title = "  Write report  ").copy(description = "  details ", notes = "   ")
        )

        val task = (outcome as Outcome.Success).value
        assertTrue(task.id.isNotBlank())
        assertEquals("Write report", task.title)
        assertEquals("details", task.description)
        assertNull(task.notes)
        assertEquals("user", task.userId)
        assertEquals(now, task.createdAt)
        assertEquals(task, repository.tasks.value.getValue(task.id))
    }

    @Test
    fun blankTitleIsRejectedAndNothingIsSaved() = runTest {
        val outcome = useCase("user", input(title = "   "))

        assertEquals(
            Outcome.Failure(TaskFailure.InvalidInput(title = TitleIssue.BLANK)),
            outcome
        )
        assertTrue(repository.tasks.value.isEmpty())
    }

    @Test
    fun aTooLongTitleIsRejected() = runTest {
        val outcome = useCase("user", input(title = "x".repeat(Task.MAX_TITLE_LENGTH + 1)))

        assertEquals(
            Outcome.Failure(TaskFailure.InvalidInput(title = TitleIssue.TOO_LONG)),
            outcome
        )
    }

    @Test
    fun durationMustBeAPositiveNumberWhenGiven() = runTest {
        assertEquals(
            Outcome.Failure(TaskFailure.InvalidInput(duration = DurationIssue.NOT_A_NUMBER)),
            useCase("user", input(duration = "abc"))
        )
        assertEquals(
            Outcome.Failure(TaskFailure.InvalidInput(duration = DurationIssue.NOT_POSITIVE)),
            useCase("user", input(duration = "0"))
        )
        val task = (useCase("user", input(duration = " 45 ")) as Outcome.Success).value
        assertEquals(45, task.estimatedDurationMinutes)
    }

    @Test
    fun reportsAllProblemsAtOnce() = runTest {
        val outcome = useCase("user", input(title = "", duration = "-3"))

        assertEquals(
            Outcome.Failure(
                TaskFailure.InvalidInput(TitleIssue.BLANK, DurationIssue.NOT_POSITIVE)
            ),
            outcome
        )
    }

    @Test
    fun editingKeepsWhatThePersonCannotChange() = runTest {
        repository.save(stored())

        val outcome = useCase(
            "user",
            input(id = "t1", title = "New", due = DueDate.AllDay(LocalDate.parse("2026-03-12")))
        )

        val task = (outcome as Outcome.Success).value
        assertEquals("t1", task.id)
        assertEquals("cat", task.categoryId)
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), task.createdAt)
        assertEquals(now, task.updatedAt)
        assertEquals(DueDate.AllDay(LocalDate.parse("2026-03-12")), task.due)
    }

    @Test
    fun editingAMissingTaskReportsNotFound() = runTest {
        assertEquals(Outcome.Failure(TaskFailure.NotFound), useCase("user", input(id = "gone")))
    }

    @Test
    fun markingATaskCompletedStampsItAndKeepsAnEarlierStamp() = runTest {
        repository.save(stored())
        val completed = (
            useCase(
                "user",
                input(id = "t1", status = TaskStatus.COMPLETED)
            ) as Outcome.Success
            ).value
        assertEquals(now, completed.completedAt)

        val earlier = Instant.parse("2026-02-01T00:00:00Z")
        repository.save(stored(TaskStatus.COMPLETED, earlier))
        val again = (
            useCase(
                "user",
                input(id = "t1", status = TaskStatus.COMPLETED)
            ) as Outcome.Success
            ).value
        assertEquals(earlier, again.completedAt)
    }

    @Test
    fun leavingTheCompletedStatusClearsTheStamp() = runTest {
        repository.save(stored(TaskStatus.COMPLETED, Instant.parse("2026-02-01T00:00:00Z")))

        val task = (
            useCase(
                "user",
                input(id = "t1", status = TaskStatus.IN_PROGRESS)
            ) as Outcome.Success
            ).value

        assertNull(task.completedAt)
    }
}
