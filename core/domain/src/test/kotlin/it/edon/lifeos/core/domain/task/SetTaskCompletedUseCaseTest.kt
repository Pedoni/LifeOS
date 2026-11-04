package it.edon.lifeos.core.domain.task

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SetTaskCompletedUseCaseTest {
    private val now = Instant.parse("2026-03-10T09:00:00Z")
    private val updatedBefore = Instant.parse("2026-01-02T00:00:00Z")

    private fun task(status: TaskStatus, completedAt: Instant? = null) = Task(
        id = "t1",
        userId = "user",
        title = "Write report",
        description = null,
        notes = null,
        status = status,
        priority = TaskPriority.MEDIUM,
        due = null,
        estimatedDurationMinutes = null,
        categoryId = null,
        completedAt = completedAt,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = updatedBefore
    )

    private fun useCase(repository: FakeTaskRepository) =
        SetTaskCompletedUseCase(repository, Clock.fixed(now, ZoneOffset.UTC))

    @Test
    fun completingStampsTheTime() = runTest {
        val repository = FakeTaskRepository(task(TaskStatus.IN_PROGRESS))

        val outcome = useCase(repository)("t1", completed = true)

        assertEquals(Outcome.Success(Unit), outcome)
        val saved = repository.tasks.value.getValue("t1")
        assertEquals(TaskStatus.COMPLETED, saved.status)
        assertEquals(now, saved.completedAt)
        assertEquals(now, saved.updatedAt)
    }

    @Test
    fun reopeningGoesBackToTodoAndClearsTheTime() = runTest {
        val repository = FakeTaskRepository(task(TaskStatus.COMPLETED, Instant.EPOCH))

        useCase(repository)("t1", completed = false)

        val saved = repository.tasks.value.getValue("t1")
        assertEquals(TaskStatus.TODO, saved.status)
        assertNull(saved.completedAt)
    }

    @Test
    fun completingACompletedTaskChangesNothing() = runTest {
        val original = task(TaskStatus.COMPLETED, Instant.EPOCH)
        val repository = FakeTaskRepository(original)

        useCase(repository)("t1", completed = true)

        assertEquals(original, repository.tasks.value.getValue("t1"))
    }

    @Test
    fun reopeningAnOpenTaskKeepsItsStatus() = runTest {
        val original = task(TaskStatus.IN_PROGRESS)
        val repository = FakeTaskRepository(original)

        useCase(repository)("t1", completed = false)

        assertEquals(original, repository.tasks.value.getValue("t1"))
    }

    @Test
    fun aMissingTaskIsReported() = runTest {
        assertEquals(
            Outcome.Failure(TaskFailure.NotFound),
            useCase(FakeTaskRepository())("missing", completed = true)
        )
    }
}
