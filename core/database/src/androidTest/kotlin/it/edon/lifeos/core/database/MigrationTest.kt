package it.edon.lifeos.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.edon.lifeos.core.database.entity.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Builds a database exactly as version 1 left it (the DDL and hash come from the exported
 * `schemas/.../1.json`), then opens it with the current code. Room refuses to open a database
 * whose tables differ from the schema it expects, so a wrong migration fails here.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "migration-test.db"

    @Before
    fun createVersion1Database() {
        context.deleteDatabase(name)
        val db = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, " +
                "`userId` TEXT NOT NULL, `name` TEXT NOT NULL, `color` TEXT, " +
                "`type` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER, " +
                "`syncStatus` TEXT NOT NULL, `remoteUpdatedAt` TEXT, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_categories_userId_type` " +
                "ON `categories` (`userId`, `type`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS room_master_table " +
                "(id INTEGER PRIMARY KEY, identity_hash TEXT)"
        )
        db.execSQL(
            "INSERT OR REPLACE INTO room_master_table (id, identity_hash) " +
                "VALUES (42, '86e12a0a6379e784d3e86d8120c6bb08')"
        )
        db.execSQL(
            "INSERT INTO categories VALUES " +
                "('c1', 'user', 'Work', NULL, 'TASK', 1, 1, NULL, 'SYNCED', 'remote')"
        )
        db.version = 1
        db.close()
    }

    @After
    fun deleteDatabase() {
        context.deleteDatabase(name)
    }

    @Test
    fun version1To2KeepsTheCategoriesAndAddsTheTasksTable() = runTest {
        val database = Room.databaseBuilder(context, LifeOSDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2)
            .build()
        try {
            assertEquals(
                listOf("Work"),
                database.categoryDao().observeActive("user").first().map { it.name }
            )

            database.taskDao().upsert(
                TaskEntity(
                    id = "t1",
                    userId = "user",
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
            )
            assertEquals("Write report", database.taskDao().getById("t1")?.title)
        } finally {
            database.close()
        }
    }
}
