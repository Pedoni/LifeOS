package it.edon.lifeos.core.data.repository

import it.edon.lifeos.core.data.mapper.toDomain
import it.edon.lifeos.core.data.mapper.toEpochMillis
import it.edon.lifeos.core.database.SyncStatus
import it.edon.lifeos.core.database.dao.TaskDao
import it.edon.lifeos.core.database.entity.TaskEntity
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.repository.TaskRepository
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Local-first, like [CategoryRepositoryImpl]: Room is written and the row marked for upload. */
internal class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao,
    private val clock: Clock
) : TaskRepository {

    override fun observeTasks(userId: String): Flow<List<Task>> =
        dao.observeActive(userId).map { entities -> entities.map(TaskEntity::toDomain) }

    override suspend fun getTask(id: String): Task? =
        dao.getById(id)?.takeIf { it.deletedAt == null }?.toDomain()

    override suspend fun save(task: Task) {
        val existing = dao.getById(task.id)
        dao.upsert(
            TaskEntity(
                id = task.id,
                userId = task.userId,
                title = task.title,
                description = task.description,
                notes = task.notes,
                status = task.status.name,
                priority = task.priority.name,
                dueDate = task.due?.toEpochMillis(),
                dueAllDay = task.due is DueDate.AllDay,
                estimatedDurationMinutes = task.estimatedDurationMinutes,
                categoryId = task.categoryId,
                completedAt = task.completedAt?.toEpochMilli(),
                createdAt = existing?.createdAt ?: task.createdAt.toEpochMilli(),
                updatedAt = clock.millis(),
                deletedAt = null,
                syncStatus = SyncStatus.PENDING_UPLOAD,
                remoteUpdatedAt = existing?.remoteUpdatedAt
            )
        )
    }

    override suspend fun delete(id: String) {
        val existing = dao.getById(id) ?: return
        if (existing.remoteUpdatedAt == null) {
            // Never reached the server: there is nothing to tell it, so no tombstone is needed.
            dao.deleteById(id)
        } else {
            val now = clock.millis()
            dao.upsert(
                existing.copy(
                    updatedAt = now,
                    deletedAt = now,
                    syncStatus = SyncStatus.PENDING_DELETE
                )
            )
        }
    }
}
