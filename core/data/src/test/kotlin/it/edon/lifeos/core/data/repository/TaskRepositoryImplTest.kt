package it.edon.lifeos.core.data.repository

import it.edon.lifeos.core.database.SyncStatus
import it.edon.lifeos.core.database.entity.TaskEntity
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskRepositoryImplTest {
    private val now = Instant.parse("2026-03-01T10:00:00Z")
    private val dao = FakeTaskDao()
    private val repository = TaskRepositoryImpl(dao, Clock.fixed(now, ZoneOffset.UTC))

    private fun task(
        id: String = "t1",
        userId: String = "user",
        due: DueDate? = null,
        status: TaskStatus = TaskStatus.TODO,
        completedAt: Instant? = null
    ) = Task(
        id = id,
        userId = userId,
        title = "Write report",
        description = "details",
        notes = null,
        status = status,
        priority = TaskPriority.HIGH,
        due = due,
        estimatedDurationMinutes = 30,
        categoryId = "cat",
        completedAt = completedAt,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-01T00:00:00Z")
    )

    private fun syncedEntity(id: String = "t1") = TaskEntity(
        id = id,
        userId = "user",
        title = "Old",
        description = null,
        notes = null,
        status = "TODO",
        priority = "LOW",
        dueDate = null,
        dueAllDay = false,
        estimatedDurationMinutes = null,
        categoryId = null,
        completedAt = null,
        createdAt = 1_000L,
        updatedAt = 2_000L,
        deletedAt = null,
        syncStatus = SyncStatus.SYNCED,
        remoteUpdatedAt = "2026-02-01T09:00:00.123456+00:00"
    )

    @Test
    fun newTaskIsStoredAsPendingUploadAndNeverSynced() = runTest {
        repository.save(task())

        val stored = dao.all.getValue("t1")
        assertEquals(SyncStatus.PENDING_UPLOAD, stored.syncStatus)
        assertNull(stored.remoteUpdatedAt)
        assertEquals(now.toEpochMilli(), stored.updatedAt)
    }

    @Test
    fun aTaskSurvivesAStoreAndLoadRoundTrip() = runTest {
        val original = task(
            due = DueDate.At(Instant.parse("2026-03-05T17:30:00Z")),
            status = TaskStatus.COMPLETED,
            completedAt = Instant.parse("2026-03-02T08:00:00Z")
        )

        repository.save(original)

        assertEquals(original.copy(updatedAt = now), repository.getTask("t1"))
    }

    @Test
    fun anAllDayDueDateIsStoredAsMidnightUtcWhateverTheTimeZone() = runTest {
        repository.save(task(due = DueDate.AllDay(LocalDate.parse("2026-03-05"))))

        val stored = dao.all.getValue("t1")
        assertTrue(stored.dueAllDay)
        assertEquals(Instant.parse("2026-03-05T00:00:00Z").toEpochMilli(), stored.dueDate)
        assertEquals(DueDate.AllDay(LocalDate.parse("2026-03-05")), repository.getTask("t1")?.due)
    }

    @Test
    fun anExactDueMomentIsNotAllDay() = runTest {
        repository.save(task(due = DueDate.At(Instant.parse("2026-03-05T00:00:00Z"))))

        assertFalse(dao.all.getValue("t1").dueAllDay)
    }

    @Test
    fun editingASyncedTaskKeepsTheServerTimestampAndCreationTime() = runTest {
        dao.upsert(syncedEntity())

        repository.save(task())

        val stored = dao.all.getValue("t1")
        assertEquals("Write report", stored.title)
        assertEquals(SyncStatus.PENDING_UPLOAD, stored.syncStatus)
        assertEquals("2026-02-01T09:00:00.123456+00:00", stored.remoteUpdatedAt)
        assertEquals(1_000L, stored.createdAt)
    }

    @Test
    fun deletingATaskThatNeverReachedTheServerRemovesIt() = runTest {
        repository.save(task())

        repository.delete("t1")

        assertTrue(dao.all.isEmpty())
    }

    @Test
    fun deletingASyncedTaskLeavesATombstoneToUpload() = runTest {
        dao.upsert(syncedEntity())

        repository.delete("t1")

        val stored = dao.all.getValue("t1")
        assertEquals(SyncStatus.PENDING_DELETE, stored.syncStatus)
        assertEquals(now.toEpochMilli(), stored.deletedAt)
        assertTrue(repository.observeTasks("user").first().isEmpty())
        assertNull(repository.getTask("t1"))
    }

    @Test
    fun deletingAnUnknownTaskDoesNothing() = runTest {
        repository.delete("missing")

        assertTrue(dao.all.isEmpty())
    }

    @Test
    fun observingReturnsOnlyTheUsersTasks() = runTest {
        repository.save(task(id = "a"))
        repository.save(task(id = "b", userId = "someone-else"))

        assertEquals(listOf("a"), repository.observeTasks("user").first().map { it.id })
    }
}
