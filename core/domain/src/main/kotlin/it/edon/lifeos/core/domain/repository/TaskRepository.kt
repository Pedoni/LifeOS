package it.edon.lifeos.core.domain.repository

import it.edon.lifeos.core.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    /**
     * Tasks of [userId] that have not been deleted. Open tasks come first, by due date with the
     * undated ones last, then the finished ones.
     */
    fun observeTasks(userId: String): Flow<List<Task>>

    suspend fun getTask(id: String): Task?

    /** Creates the task, or updates it when the id already exists. */
    suspend fun save(task: Task)

    suspend fun delete(id: String)
}
