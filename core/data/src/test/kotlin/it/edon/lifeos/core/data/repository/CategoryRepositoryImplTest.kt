package it.edon.lifeos.core.data.repository

import it.edon.lifeos.core.database.SyncStatus
import it.edon.lifeos.core.database.entity.CategoryEntity
import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.model.CategoryType
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryRepositoryImplTest {
    private val now = Instant.parse("2026-03-01T10:00:00Z")
    private val dao = FakeCategoryDao()
    private val repository = CategoryRepositoryImpl(dao, Clock.fixed(now, ZoneOffset.UTC))

    private fun category(id: String = "c1", name: String = "Work", userId: String = "user") =
        Category(
            id = id,
            userId = userId,
            name = name,
            color = null,
            type = CategoryType.TASK,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-01T00:00:00Z")
        )

    private fun syncedEntity(id: String = "c1") = CategoryEntity(
        id = id,
        userId = "user",
        name = "Old",
        color = null,
        type = "TASK",
        createdAt = 1_000L,
        updatedAt = 2_000L,
        deletedAt = null,
        syncStatus = SyncStatus.SYNCED,
        remoteUpdatedAt = "2026-02-01T09:00:00.123456+00:00"
    )

    @Test
    fun newCategoryIsStoredAsPendingUploadAndNeverSynced() = runTest {
        repository.save(category())

        val stored = dao.all.getValue("c1")
        assertEquals(SyncStatus.PENDING_UPLOAD, stored.syncStatus)
        assertNull(stored.remoteUpdatedAt)
        assertEquals(now.toEpochMilli(), stored.updatedAt)
        assertEquals(listOf("Work"), repository.observeCategories("user").first().map { it.name })
    }

    @Test
    fun editingASyncedCategoryKeepsTheServerTimestampAndCreationTime() = runTest {
        dao.upsert(syncedEntity())

        repository.save(category(name = "New"))

        val stored = dao.all.getValue("c1")
        assertEquals("New", stored.name)
        assertEquals(SyncStatus.PENDING_UPLOAD, stored.syncStatus)
        assertEquals("2026-02-01T09:00:00.123456+00:00", stored.remoteUpdatedAt)
        assertEquals(1_000L, stored.createdAt)
        assertEquals(now.toEpochMilli(), stored.updatedAt)
    }

    @Test
    fun deletingACategoryThatNeverReachedTheServerRemovesIt() = runTest {
        repository.save(category())

        repository.delete("c1")

        assertTrue(dao.all.isEmpty())
    }

    @Test
    fun deletingASyncedCategoryLeavesATombstoneToUpload() = runTest {
        dao.upsert(syncedEntity())

        repository.delete("c1")

        val stored = dao.all.getValue("c1")
        assertEquals(SyncStatus.PENDING_DELETE, stored.syncStatus)
        assertEquals(now.toEpochMilli(), stored.deletedAt)
        assertTrue(repository.observeCategories("user").first().isEmpty())
    }

    @Test
    fun deletingAnUnknownCategoryDoesNothing() = runTest {
        repository.delete("missing")

        assertTrue(dao.all.isEmpty())
    }

    @Test
    fun observingReturnsOnlyTheUsersCategoriesOrderedByName() = runTest {
        repository.save(category(id = "a", name = "banana"))
        repository.save(category(id = "b", name = "Apple"))
        repository.save(category(id = "c", name = "Other", userId = "someone-else"))

        val names = repository.observeCategories("user").first().map { it.name }

        assertEquals(listOf("Apple", "banana"), names)
    }
}
