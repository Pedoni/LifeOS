package it.edon.lifeos.core.domain.task

import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

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
