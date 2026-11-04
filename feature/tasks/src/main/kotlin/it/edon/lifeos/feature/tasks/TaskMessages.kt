package it.edon.lifeos.feature.tasks

import androidx.annotation.StringRes
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import it.edon.lifeos.core.domain.task.SortDirection
import it.edon.lifeos.core.domain.task.TaskSortField
import it.edon.lifeos.core.domain.task.TaskView

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

@StringRes
internal fun TaskView.labelRes(): Int = when (this) {
    TaskView.ALL -> R.string.tasks_view_all
    TaskView.TODAY -> R.string.tasks_view_today
    TaskView.UPCOMING -> R.string.tasks_view_upcoming
    TaskView.OVERDUE -> R.string.tasks_view_overdue
    TaskView.COMPLETED -> R.string.tasks_view_completed
}

@StringRes
internal fun TaskSortField.labelRes(): Int = when (this) {
    TaskSortField.DUE_DATE -> R.string.tasks_sort_due_date
    TaskSortField.PRIORITY -> R.string.tasks_sort_priority
    TaskSortField.CREATED -> R.string.tasks_sort_created
    TaskSortField.TITLE -> R.string.tasks_sort_title
}

@StringRes
internal fun SortDirection.labelRes(): Int = when (this) {
    SortDirection.ASCENDING -> R.string.tasks_sort_ascending
    SortDirection.DESCENDING -> R.string.tasks_sort_descending
}
