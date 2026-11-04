package it.edon.lifeos.core.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskTest {
    private val now = Instant.parse("2026-03-10T23:30:00Z")
    private val utc = ZoneId.of("UTC")
    private val tokyo = ZoneId.of("Asia/Tokyo")

    private fun task(
        title: String = "Write report",
        status: TaskStatus = TaskStatus.TODO,
        completedAt: Instant? = null,
        due: DueDate? = null,
        duration: Int? = null
    ) = Task(
        id = "t1",
        userId = "user",
        title = title,
        description = null,
        notes = null,
        status = status,
        priority = TaskPriority.MEDIUM,
        due = due,
        estimatedDurationMinutes = duration,
        categoryId = null,
        completedAt = completedAt,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    @Test(expected = IllegalArgumentException::class)
    fun blankTitleIsRejected() {
        task(title = "  ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun tooLongTitleIsRejected() {
        task(title = "x".repeat(Task.MAX_TITLE_LENGTH + 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonPositiveDurationIsRejected() {
        task(duration = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun completedTaskNeedsACompletionTime() {
        task(status = TaskStatus.COMPLETED)
    }

    @Test(expected = IllegalArgumentException::class)
    fun openTaskCannotHaveACompletionTime() {
        task(completedAt = Instant.EPOCH)
    }

    @Test
    fun aTaskWithoutDueDateIsNeverOverdue() {
        assertFalse(task().isOverdue(now, utc))
    }

    @Test
    fun anExactMomentInThePastIsOverdue() {
        assertTrue(task(due = DueDate.At(now.minusSeconds(1))).isOverdue(now, utc))
        assertFalse(task(due = DueDate.At(now.plusSeconds(1))).isOverdue(now, utc))
    }

    @Test
    fun aDayIsOverdueOnlyOnceItIsOver() {
        val today = DueDate.AllDay(LocalDate.parse("2026-03-10"))
        val yesterday = DueDate.AllDay(LocalDate.parse("2026-03-09"))

        assertFalse(task(due = today).isOverdue(now, utc))
        assertTrue(task(due = yesterday).isOverdue(now, utc))
    }

    @Test
    fun theDayDependsOnTheTimeZoneOfThePerson() {
        // It is already March 11th in Tokyo.
        val today = DueDate.AllDay(LocalDate.parse("2026-03-10"))

        assertTrue(task(due = today).isOverdue(now, tokyo))
    }

    @Test
    fun finishedTasksAreNeverOverdue() {
        val past = DueDate.At(Instant.EPOCH)

        assertFalse(
            task(
                status = TaskStatus.COMPLETED,
                completedAt = Instant.EPOCH,
                due = past
            ).isOverdue(now, utc)
        )
        assertFalse(task(status = TaskStatus.CANCELLED, due = past).isOverdue(now, utc))
        assertTrue(task(status = TaskStatus.IN_PROGRESS, due = past).isOverdue(now, utc))
    }
}
