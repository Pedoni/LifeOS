package it.edon.lifeos.core.domain.task

import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus

/** What a person fills in to create or edit a task. [id] is null for a new task. */
data class TaskInput(
    val id: String?,
    val title: String,
    val description: String,
    val notes: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val due: DueDate?,
    val categoryId: String?,
    /** Kept as typed, so a wrong value can be reported instead of silently dropped. */
    val estimatedDurationMinutes: String
)

enum class TitleIssue { BLANK, TOO_LONG }

enum class DurationIssue { NOT_A_NUMBER, NOT_POSITIVE }

/** Why saving a task did not go through. */
sealed interface TaskFailure {
    /** Nothing was saved: at least one field broke a rule. */
    data class InvalidInput(val title: TitleIssue? = null, val duration: DurationIssue? = null) :
        TaskFailure

    /** The task being edited no longer exists (for example it was deleted meanwhile). */
    data object NotFound : TaskFailure
}
