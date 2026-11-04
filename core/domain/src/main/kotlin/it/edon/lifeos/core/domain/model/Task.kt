package it.edon.lifeos.core.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    /** Open tasks are the ones still waiting to be done. */
    val isOpen: Boolean get() = this == TODO || this == IN_PROGRESS
}

enum class TaskPriority { LOW, MEDIUM, HIGH, URGENT }

/** A task is due either on a calendar day, or at an exact moment. */
sealed interface DueDate {
    /** "By Friday": the day does not move when the device changes time zone. */
    data class AllDay(val date: LocalDate) : DueDate

    data class At(val instant: Instant) : DueDate
}

/**
 * Something to do. The rules mirror the backend constraints, so a task that exists in the domain
 * can always be synchronized.
 */
data class Task(
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val notes: String?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val due: DueDate?,
    val estimatedDurationMinutes: Int?,
    val categoryId: String?,
    val completedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(title.isNotBlank() && title.trim().length <= MAX_TITLE_LENGTH) {
            "Task title must have between 1 and $MAX_TITLE_LENGTH characters"
        }
        require(estimatedDurationMinutes == null || estimatedDurationMinutes > 0) {
            "Estimated duration must be positive"
        }
        require((status == TaskStatus.COMPLETED) == (completedAt != null)) {
            "A task has a completion time exactly when it is completed"
        }
    }

    /** Whether the task is still open and its due date has passed. */
    fun isOverdue(now: Instant, zone: ZoneId): Boolean {
        if (!status.isOpen) return false
        return when (val due = due) {
            null -> false
            is DueDate.AllDay -> due.date.isBefore(now.atZone(zone).toLocalDate())
            is DueDate.At -> due.instant.isBefore(now)
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 300
    }
}
