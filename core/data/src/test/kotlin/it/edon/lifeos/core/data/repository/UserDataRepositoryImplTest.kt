package it.edon.lifeos.core.data.repository

import it.edon.lifeos.core.database.SyncStatus
import it.edon.lifeos.core.database.entity.CategoryEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UserDataRepositoryImplTest {
    private val dao = FakeCategoryDao()
    private val repository = UserDataRepositoryImpl(dao)

    private fun entity(id: String, userId: String) = CategoryEntity(
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

    @Test
    fun deletesOnlyTheRowsOfThatUser() = runTest {
        dao.upsert(entity("a", "user"))
        dao.upsert(entity("b", "other"))

        repository.deleteAllFor("user")

        assertEquals(setOf("b"), dao.all.keys)
    }
}
