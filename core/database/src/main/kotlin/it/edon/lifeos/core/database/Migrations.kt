package it.edon.lifeos.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the `tasks` table. Existing data is untouched. */
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `tasks` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, " +
                "`title` TEXT NOT NULL, `description` TEXT, `notes` TEXT, " +
                "`status` TEXT NOT NULL, `priority` TEXT NOT NULL, `dueDate` INTEGER, " +
                "`dueAllDay` INTEGER NOT NULL, `estimatedDurationMinutes` INTEGER, " +
                "`categoryId` TEXT, `completedAt` INTEGER, `createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER, " +
                "`syncStatus` TEXT NOT NULL, `remoteUpdatedAt` TEXT, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_tasks_userId_status` ON `tasks` (`userId`, `status`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_tasks_userId_dueDate` ON `tasks` (`userId`, `dueDate`)"
        )
    }
}
