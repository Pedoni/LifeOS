package it.edon.lifeos.core.data.repository

import it.edon.lifeos.core.database.dao.CategoryDao
import it.edon.lifeos.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory DAO with the same visible behavior as the Room one, for JVM tests. */
internal class FakeCategoryDao : CategoryDao {
    private val rows = MutableStateFlow<Map<String, CategoryEntity>>(emptyMap())

    val all: Map<String, CategoryEntity> get() = rows.value

    override fun observeActive(userId: String): Flow<List<CategoryEntity>> = rows.map { map ->
        map.values
            .filter { it.userId == userId && it.deletedAt == null }
            .sortedBy { it.name.lowercase() }
    }

    override suspend fun getById(id: String): CategoryEntity? = rows.value[id]

    override suspend fun upsert(entity: CategoryEntity) {
        rows.value = rows.value + (entity.id to entity)
    }

    override suspend fun deleteById(id: String) {
        rows.value = rows.value - id
    }

    override suspend fun deleteAllForUser(userId: String) {
        rows.value = rows.value.filterValues { it.userId != userId }
    }
}
