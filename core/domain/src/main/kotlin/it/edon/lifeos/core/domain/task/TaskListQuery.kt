package it.edon.lifeos.core.domain.task

import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** Which part of the list is shown. The views other than [ALL] do not overlap. */
enum class TaskView {
    ALL,

    /** Open tasks due today that are not late yet. */
    TODAY,

    /** Open tasks due from tomorrow on. */
    UPCOMING,

    /** Open tasks whose due date has passed. */
    OVERDUE,

    COMPLETED
}

enum class TaskSortField { DUE_DATE, PRIORITY, CREATED, TITLE }

enum class SortDirection { ASCENDING, DESCENDING }

/**
 * Ascending is the natural order of the field: soonest due date, lowest priority, oldest, A to Z.
 * Whatever the field, finished tasks come after open ones, and tasks without due date after the
 * dated ones. A task due on a whole day counts as due at the end of that day.
 */
data class TaskSort(
    val field: TaskSortField = TaskSortField.DUE_DATE,
    val direction: SortDirection = SortDirection.ASCENDING
)

/** An empty set of [priorities] and a null [categoryId] mean "any". */
data class TaskFilter(
    val priorities: Set<TaskPriority> = emptySet(),
    val categoryId: String? = null
) {
    val isActive: Boolean get() = priorities.isNotEmpty() || categoryId != null
}

/** Tasks that share a priority. [priority] is null when the list is not grouped. */
data class TaskGroup(val priority: TaskPriority?, val tasks: List<Task>)

/** What the person asked to see: a view, a filter, a sort order and whether to group. */
data class TaskListQuery(
    val view: TaskView = TaskView.ALL,
    val filter: TaskFilter = TaskFilter(),
    val sort: TaskSort = TaskSort(),
    val groupByPriority: Boolean = false
) {
    /** Whether anything differs from the plain list, so the person can be offered a reset. */
    val isCustomized: Boolean get() = this != TaskListQuery()

    /** Applies the query to [tasks]. Empty groups are left out; the order is deterministic. */
    fun apply(tasks: List<Task>, now: Instant, zone: ZoneId): List<TaskGroup> {
        val selected = tasks
            .filter { it.isIn(view, now, zone) && filter.accepts(it) }
            .sortedWith(sort.comparator(zone))
        return if (groupByPriority) {
            TaskPriority.entries.reversed().mapNotNull { priority ->
                selected.filter { it.priority == priority }
                    .takeIf { it.isNotEmpty() }
                    ?.let { TaskGroup(priority, it) }
            }
        } else {
            listOfNotNull(selected.takeIf { it.isNotEmpty() }?.let { TaskGroup(null, it) })
        }
    }
}

private fun Task.isIn(view: TaskView, now: Instant, zone: ZoneId): Boolean = when (view) {
    TaskView.ALL -> true
    TaskView.COMPLETED -> status == TaskStatus.COMPLETED
    TaskView.OVERDUE -> isOverdue(now, zone)
    TaskView.TODAY -> dayRelativeToToday(now, zone) == 0
    TaskView.UPCOMING -> dayRelativeToToday(now, zone)?.let { it > 0 } ?: false
}

/** Days from today to the due day, only for open tasks that are not late yet. */
private fun Task.dayRelativeToToday(now: Instant, zone: ZoneId): Int? {
    val due = due?.takeIf { status.isOpen && !isOverdue(now, zone) } ?: return null
    return due.day(zone).compareTo(now.atZone(zone).toLocalDate()).coerceIn(-1, 1)
}

private fun TaskFilter.accepts(task: Task): Boolean =
    (priorities.isEmpty() || task.priority in priorities) &&
        (categoryId == null || task.categoryId == categoryId)

private fun TaskSort.comparator(zone: ZoneId): Comparator<Task> {
    val sign = if (direction == SortDirection.ASCENDING) 1 else -1
    val byField: Comparator<Task> = when (field) {
        TaskSortField.DUE_DATE -> Comparator { a, b -> compareDue(a.due, b.due, zone, sign) }

        TaskSortField.PRIORITY -> Comparator { a, b -> sign * a.priority.compareTo(b.priority) }

        TaskSortField.CREATED -> Comparator { a, b -> sign * a.createdAt.compareTo(b.createdAt) }

        TaskSortField.TITLE ->
            Comparator { a, b -> sign * a.title.compareTo(b.title, ignoreCase = true) }
    }
    return compareBy<Task> { !it.status.isOpen }
        .then(byField)
        // Newest first and then by id, so equal tasks never swap places between refreshes.
        .thenByDescending { it.createdAt }
        .thenBy { it.id }
}

// Undated tasks go last in both directions.
private fun compareDue(a: DueDate?, b: DueDate?, zone: ZoneId, sign: Int): Int = when {
    a == null && b == null -> 0
    a == null -> 1
    b == null -> -1
    else -> sign * a.moment(zone).compareTo(b.moment(zone))
}

private fun DueDate.moment(zone: ZoneId): LocalDateTime = when (this) {
    is DueDate.AllDay -> date.atTime(LocalTime.MAX)
    is DueDate.At -> instant.atZone(zone).toLocalDateTime()
}
