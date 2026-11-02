package it.edon.lifeos.core.data.repository

import it.edon.lifeos.core.data.mapper.toDomain
import it.edon.lifeos.core.database.SyncStatus
import it.edon.lifeos.core.database.dao.CategoryDao
import it.edon.lifeos.core.database.entity.CategoryEntity
import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.repository.CategoryRepository
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local-first: every change is written to Room and marked for upload. Sending it to the
 * backend is the job of the synchronization layer, not of this repository.
 */
internal class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    private val clock: Clock
) : CategoryRepository {

    override fun observeCategories(userId: String): Flow<List<Category>> =
        dao.observeActive(userId).map { entities -> entities.map(CategoryEntity::toDomain) }

    override suspend fun save(category: Category) {
        val existing = dao.getById(category.id)
        dao.upsert(
            CategoryEntity(
                id = category.id,
                userId = category.userId,
                name = category.name,
                color = category.color,
                type = category.type.name,
                createdAt = existing?.createdAt ?: category.createdAt.toEpochMilli(),
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
