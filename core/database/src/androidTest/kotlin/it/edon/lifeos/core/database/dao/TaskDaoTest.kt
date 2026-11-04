package it.edon.lifeos.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.edon.lifeos.core.database.LifeOSDatabase
import it.edon.lifeos.core.database.SyncStatus
import it.edon.lifeos.core.database.entity.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var database: LifeOSDatabase
    private lateinit var dao: TaskDao

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LifeOSDatabase::class.java
        ).build()
        dao = database.taskDao()
    }

    @After
    fun closeDatabase() = database.close()

    private fun entity(
        id: String,
        userId: String = "user",
        status: String = "TODO",
        dueDate: Long? = null,
        createdAt: Long = 1L,
        deletedAt: Long? = null
    ) = TaskEntity(
        id = id,
        userId = userId,
        title = "Task $id",
        description = null,
        notes = null,
        status = status,
        priority = "MEDIUM",
        dueDate = dueDate,
        dueAllDay = false,
        estimatedDurationMinutes = null,
        categoryId = null,
        completedAt = null,
        createdAt = createdAt,
        updatedAt = 1L,
        deletedAt = deletedAt,
        syncStatus = SyncStatus.PENDING_UPLOAD,
        remoteUpdatedAt = null
    )

    private suspend fun observedIds() = dao.observeActive("user").first().map { it.id }

    @Test
    fun openTasksComeFirstByDueDateThenUndatedThenFinished() = runTest {
        dao.upsert(entity("done", status = "COMPLETED", dueDate = 1L))
        dao.upsert(entity("undated-old", createdAt = 1L))
        dao.upsert(entity("undated-new", createdAt = 2L))
        dao.upsert(entity("later", dueDate = 200L))
        dao.upsert(entity("sooner", status = "IN_PROGRESS", dueDate = 100L))
        dao.upsert(entity("cancelled", status = "CANCELLED"))

        val ids = observedIds()

        assertEquals(listOf("sooner", "later", "undated-new", "undated-old"), ids.take(4))
        assertEquals(setOf("done", "cancelled"), ids.drop(4).toSet())
    }

    @Test
    fun softDeletedAndOtherUsersTasksAreNotObserved() = runTest {
        dao.upsert(entity("a"))
        dao.upsert(entity("b", deletedAt = 5L))
        dao.upsert(entity("c", userId = "someone-else"))

        assertEquals(listOf("a"), observedIds())
    }

    @Test
    fun upsertReplacesTheRowWithTheSameId() = runTest {
        dao.upsert(entity("a", status = "TODO"))
        dao.upsert(entity("a", status = "COMPLETED"))

        assertEquals("COMPLETED", dao.getById("a")?.status)
    }

    @Test
    fun deletingAllForAUserLeavesTheOthersAlone() = runTest {
        dao.upsert(entity("a"))
        dao.upsert(entity("b", deletedAt = 5L))
        dao.upsert(entity("c", userId = "other"))

        dao.deleteAllForUser("user")

        assertNull(dao.getById("a"))
        assertNull(dao.getById("b"))
        assertEquals(listOf("c"), dao.observeActive("other").first().map { it.id })
    }

    @Test
    fun deletedRowIsGone() = runTest {
        dao.upsert(entity("a"))

        dao.deleteById("a")

        assertNull(dao.getById("a"))
    }
}
