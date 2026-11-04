package it.edon.lifeos.feature.tasks

import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import it.edon.lifeos.core.domain.repository.TaskRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal fun testTask(
    id: String = "t1",
    title: String = "Write report",
    status: TaskStatus = TaskStatus.TODO,
    completedAt: Instant? = null,
    due: DueDate? = null,
    priority: TaskPriority = TaskPriority.HIGH,
    categoryId: String? = null
) = Task(
    id = id,
    userId = "u1",
    title = title,
    description = "details",
    notes = null,
    status = status,
    priority = priority,
    due = due,
    estimatedDurationMinutes = 30,
    categoryId = categoryId,
    completedAt = completedAt,
    createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    updatedAt = Instant.parse("2026-01-01T00:00:00Z")
)

internal class FakeTaskRepository(vararg initial: Task) : TaskRepository {
    val tasks = MutableStateFlow(initial.associateBy { it.id })

    override fun observeTasks(userId: String): Flow<List<Task>> =
        tasks.map { map -> map.values.filter { it.userId == userId } }

    override suspend fun getTask(id: String): Task? = tasks.value[id]

    override suspend fun save(task: Task) {
        tasks.value = tasks.value + (task.id to task)
    }

    override suspend fun delete(id: String) {
        tasks.value = tasks.value - id
    }
}
