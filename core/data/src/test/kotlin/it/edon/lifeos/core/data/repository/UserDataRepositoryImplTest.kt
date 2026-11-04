package it.edon.lifeos.core.data.repository

import it.edon.lifeos.core.database.SyncStatus
import it.edon.lifeos.core.database.entity.CategoryEntity
import it.edon.lifeos.core.database.entity.TaskEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UserDataRepositoryImplTest {
    private val categories = FakeCategoryDao()
    private val tasks = FakeTaskDao()
    private val repository = UserDataRepositoryImpl(categories, tasks)

    private fun category(id: String, userId: String) = CategoryEntity(
        id = id,
        userId = userId,
        name = "Work",
        color = null,
        type = "TASK",
        createdAt = 1L,
        updatedAt = 1L,
        deletedAt = null,
        syncStatus = SyncStatus.PENDING_UPLOAD,
        remoteUpdatedAt = null
    )

    private fun task(id: String, userId: String) = TaskEntity(
        id = id,
        userId = userId,
        title = "Write report",
        description = null,
        notes = null,
        status = "TODO",
        priority = "MEDIUM",
        dueDate = null,
        dueAllDay = false,
        estimatedDurationMinutes = null,
        categoryId = null,
        completedAt = null,
        createdAt = 1L,
        updatedAt = 1L,
        deletedAt = null,
        syncStatus = SyncStatus.PENDING_UPLOAD,
        remoteUpdatedAt = null
    )

    @Test
    fun deletesOnlyTheRowsOfThatUserInEveryTable() = runTest {
        categories.upsert(category("a", "user"))
        categories.upsert(category("b", "other"))
        tasks.upsert(task("t1", "user"))
        tasks.upsert(task("t2", "other"))

        repository.deleteAllFor("user")

        assertEquals(setOf("b"), categories.all.keys)
        assertEquals(setOf("t2"), tasks.all.keys)
    }
}
