package it.edon.lifeos.core.data.repository

import it.edon.lifeos.core.database.dao.TaskDao
import it.edon.lifeos.core.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory DAO with the same visible filtering as the Room one, for JVM tests. */
internal class FakeTaskDao : TaskDao {
    private val rows = MutableStateFlow<Map<String, TaskEntity>>(emptyMap())

    val all: Map<String, TaskEntity> get() = rows.value

    override fun observeActive(userId: String): Flow<List<TaskEntity>> = rows.map { map ->
        map.values.filter { it.userId == userId && it.deletedAt == null }
    }

    override suspend fun getById(id: String): TaskEntity? = rows.value[id]

    override suspend fun upsert(entity: TaskEntity) {
        rows.value = rows.value + (entity.id to entity)
    }

    override suspend fun deleteById(id: String) {
        rows.value = rows.value - id
    }

    override suspend fun deleteAllForUser(userId: String) {
        rows.value = rows.value.filterValues { it.userId != userId }
    }
}
