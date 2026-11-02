package it.edon.lifeos.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.edon.lifeos.core.database.LifeOSDatabase
import it.edon.lifeos.core.database.SyncStatus
import it.edon.lifeos.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {
    private lateinit var database: LifeOSDatabase
    private lateinit var dao: CategoryDao

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LifeOSDatabase::class.java
        ).build()
        dao = database.categoryDao()
    }

    @After
    fun closeDatabase() = database.close()

    private fun entity(
        id: String,
        name: String = "Work",
        userId: String = "user",
        deletedAt: Long? = null
    ) = CategoryEntity(
        id = id,
        userId = userId,
        name = name,
        color = null,
        type = "TASK",
        createdAt = 1L,
        updatedAt = 1L,
        deletedAt = deletedAt,
        syncStatus = SyncStatus.PENDING_UPLOAD,
        remoteUpdatedAt = null
    )

    @Test
    fun observedRowsAreOrderedByNameIgnoringCase() = runTest {
        dao.upsert(entity("a", name = "Zed"))
        dao.upsert(entity("b", name = "alpha"))

        assertEquals(listOf("alpha", "Zed"), dao.observeActive("user").first().map { it.name })
    }

    @Test
    fun softDeletedRowsAreNotObserved() = runTest {
        dao.upsert(entity("a"))
        dao.upsert(entity("b", deletedAt = 5L))

        assertEquals(listOf("a"), dao.observeActive("user").first().map { it.id })
    }

    @Test
    fun otherUsersRowsAreNotObserved() = runTest {
        dao.upsert(entity("a"))
        dao.upsert(entity("b", userId = "someone-else"))

        assertEquals(listOf("a"), dao.observeActive("user").first().map { it.id })
    }

    @Test
    fun upsertReplacesTheRowWithTheSameId() = runTest {
        dao.upsert(entity("a", name = "Old"))
        dao.upsert(entity("a", name = "New"))

        assertEquals("New", dao.getById("a")?.name)
    }

    @Test
    fun deletedRowIsGone() = runTest {
        dao.upsert(entity("a"))

        dao.deleteById("a")

        assertNull(dao.getById("a"))
    }
}
