package it.edon.lifeos.core.domain.task

import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskListQueryTest {
    // 23:30 UTC on March 10th is already March 11th in Rome (UTC+1).
    private val now = Instant.parse("2026-03-10T23:30:00Z")
    private val utc = ZoneId.of("UTC")
    private val rome = ZoneId.of("Europe/Rome")

    private fun day(date: String) = DueDate.AllDay(LocalDate.parse(date))

    private fun at(instant: String) = DueDate.At(Instant.parse(instant))

    private fun task(
        id: String,
        due: DueDate? = null,
        status: TaskStatus = TaskStatus.TODO,
        priority: TaskPriority = TaskPriority.MEDIUM,
        title: String = id,
        categoryId: String? = null,
        created: String = "2026-01-01T00:00:00Z"
    ) = Task(
        id = id,
        userId = "user",
        title = title,
        description = null,
        notes = null,
        status = status,
        priority = priority,
        due = due,
        estimatedDurationMinutes = null,
        categoryId = categoryId,
        completedAt = if (status == TaskStatus.COMPLETED) Instant.EPOCH else null,
        createdAt = Instant.parse(created),
        updatedAt = Instant.EPOCH
    )

    private fun ids(query: TaskListQuery, tasks: List<Task>, zone: ZoneId = utc) =
        query.apply(tasks, now, zone).flatMap { group -> group.tasks.map { it.id } }

    private val sample = listOf(
        task("late", due = day("2026-03-09")),
        task("today", due = day("2026-03-10")),
        task("tomorrow", due = day("2026-03-11")),
        task("later", due = at("2026-04-01T10:00:00Z")),
        task("undated"),
        task("done", status = TaskStatus.COMPLETED, due = day("2026-03-10")),
        task("cancelled", status = TaskStatus.CANCELLED)
    )

    @Test
    fun allShowsEverything() {
        assertEquals(sample.size, ids(TaskListQuery(), sample).size)
    }

    @Test
    fun todayHoldsOpenTasksDueTodayThatAreNotLate() {
        assertEquals(listOf("today"), ids(TaskListQuery(view = TaskView.TODAY), sample))
    }

    @Test
    fun aMomentAlreadyPassedTodayIsOverdueNotToday() {
        val tasks = listOf(
            task("passed", due = at("2026-03-10T08:00:00Z")),
            task("coming", due = at("2026-03-10T23:45:00Z"))
        )

        assertEquals(listOf("coming"), ids(TaskListQuery(view = TaskView.TODAY), tasks))
        assertEquals(listOf("passed"), ids(TaskListQuery(view = TaskView.OVERDUE), tasks))
    }

    @Test
    fun upcomingStartsTomorrow() {
        assertEquals(
            listOf("tomorrow", "later"),
            ids(TaskListQuery(view = TaskView.UPCOMING), sample)
        )
    }

    @Test
    fun overdueHoldsOnlyOpenLateTasks() {
        assertEquals(listOf("late"), ids(TaskListQuery(view = TaskView.OVERDUE), sample))
    }

    @Test
    fun completedHoldsOnlyCompletedTasks() {
        assertEquals(listOf("done"), ids(TaskListQuery(view = TaskView.COMPLETED), sample))
    }

    @Test
    fun theViewsOtherThanAllDoNotOverlap() {
        val views = listOf(TaskView.TODAY, TaskView.UPCOMING, TaskView.OVERDUE, TaskView.COMPLETED)
        val all = views.flatMap { ids(TaskListQuery(view = it), sample) }

        assertEquals(all.size, all.toSet().size)
    }

    @Test
    fun todayDependsOnTheTimeZoneOfThePerson() {
        // In Rome it is already the 11th: the task due the 10th is late, the one due the 11th is today.
        assertEquals(listOf("tomorrow"), ids(TaskListQuery(view = TaskView.TODAY), sample, rome))
        assertTrue("late" in ids(TaskListQuery(view = TaskView.OVERDUE), sample, rome))
        assertTrue("today" in ids(TaskListQuery(view = TaskView.OVERDUE), sample, rome))
    }

    @Test
    fun filtersByPriorityAndCategory() {
        val tasks = listOf(
            task("a", priority = TaskPriority.HIGH, categoryId = "work"),
            task("b", priority = TaskPriority.LOW, categoryId = "work"),
            task("c", priority = TaskPriority.HIGH, categoryId = "home")
        )

        val byPriority = TaskFilter(priorities = setOf(TaskPriority.HIGH, TaskPriority.URGENT))
        assertEquals(listOf("a", "c"), ids(TaskListQuery(filter = byPriority), tasks).sorted())
        val both = byPriority.copy(categoryId = "work")
        assertEquals(listOf("a"), ids(TaskListQuery(filter = both), tasks))
    }

    @Test
    fun anEmptyFilterIsNotActive() {
        assertFalse(TaskFilter().isActive)
        assertTrue(TaskFilter(categoryId = "x").isActive)
        assertFalse(TaskListQuery().isCustomized)
        assertTrue(TaskListQuery(groupByPriority = true).isCustomized)
    }

    @Test
    fun sortsByDueDateWithUndatedAndFinishedTasksLast() {
        val tasks = listOf(
            task("undated"),
            task("done", status = TaskStatus.COMPLETED, due = day("2026-03-01")),
            task("b", due = day("2026-03-20")),
            task("a", due = at("2026-03-20T10:00:00Z")),
            task("c", due = day("2026-03-15"))
        )

        // A whole day counts as the end of that day, so the timed task of the same day comes first.
        assertEquals(listOf("c", "a", "b", "undated", "done"), ids(TaskListQuery(), tasks))

        val descending = TaskListQuery(sort = TaskSort(direction = SortDirection.DESCENDING))
        assertEquals(listOf("b", "a", "c", "undated", "done"), ids(descending, tasks))
    }

    @Test
    fun sortsByPriorityTitleAndCreation() {
        val tasks = listOf(
            task(
                "1",
                priority = TaskPriority.LOW,
                title = "banana",
                created = "2026-01-03T00:00:00Z"
            ),
            task(
                "2",
                priority = TaskPriority.URGENT,
                title = "Apple",
                created = "2026-01-01T00:00:00Z"
            ),
            task(
                "3",
                priority = TaskPriority.MEDIUM,
                title = "cherry",
                created = "2026-01-02T00:00:00Z"
            )
        )
        fun sorted(field: TaskSortField, direction: SortDirection) =
            ids(TaskListQuery(sort = TaskSort(field, direction)), tasks)

        assertEquals(listOf("1", "3", "2"), sorted(TaskSortField.PRIORITY, SortDirection.ASCENDING))
        assertEquals(
            listOf("2", "3", "1"),
            sorted(TaskSortField.PRIORITY, SortDirection.DESCENDING)
        )
        assertEquals(listOf("2", "1", "3"), sorted(TaskSortField.TITLE, SortDirection.ASCENDING))
        assertEquals(listOf("2", "3", "1"), sorted(TaskSortField.CREATED, SortDirection.ASCENDING))
    }

    @Test
    fun tasksThatTieKeepAStableOrder() {
        val tasks = listOf(task("b"), task("a"), task("c"))

        assertEquals(listOf("a", "b", "c"), ids(TaskListQuery(), tasks))
        assertEquals(listOf("a", "b", "c"), ids(TaskListQuery(), tasks.reversed()))
    }

    @Test
    fun groupsByPriorityFromUrgentToLowAndLeavesEmptyGroupsOut() {
        val tasks = listOf(
            task("low", priority = TaskPriority.LOW),
            task("urgent", priority = TaskPriority.URGENT),
            task("low2", priority = TaskPriority.LOW)
        )

        val groups = TaskListQuery(groupByPriority = true).apply(tasks, now, utc)

        assertEquals(listOf(TaskPriority.URGENT, TaskPriority.LOW), groups.map { it.priority })
        assertEquals(listOf("low", "low2"), groups.last().tasks.map { it.id })
    }

    @Test
    fun nothingMatchingGivesNoGroups() {
        assertTrue(TaskListQuery(view = TaskView.OVERDUE).apply(emptyList(), now, utc).isEmpty())
        assertTrue(TaskListQuery().apply(emptyList(), now, utc).isEmpty())
    }

    @Test
    fun aListWithoutGroupingIsASingleGroupWithoutPriority() {
        val groups = TaskListQuery().apply(listOf(task("a")), now, utc)

        assertEquals(1, groups.size)
        assertEquals(null, groups.single().priority)
    }
}
