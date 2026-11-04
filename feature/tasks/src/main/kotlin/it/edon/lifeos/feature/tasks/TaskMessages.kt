package it.edon.lifeos.feature.tasks

import androidx.annotation.StringRes
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus

@StringRes
internal fun TaskStatus.labelRes(): Int = when (this) {
    TaskStatus.TODO -> R.string.tasks_status_todo
    TaskStatus.IN_PROGRESS -> R.string.tasks_status_in_progress
    TaskStatus.COMPLETED -> R.string.tasks_status_completed
    TaskStatus.CANCELLED -> R.string.tasks_status_cancelled
}

@StringRes
internal fun TaskPriority.labelRes(): Int = when (this) {
    TaskPriority.LOW -> R.string.tasks_priority_low
    TaskPriority.MEDIUM -> R.string.tasks_priority_medium
    TaskPriority.HIGH -> R.string.tasks_priority_high
    TaskPriority.URGENT -> R.string.tasks_priority_urgent
}
