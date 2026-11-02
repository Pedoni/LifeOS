package it.edon.lifeos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import it.edon.lifeos.core.database.dao.CategoryDao
import it.edon.lifeos.core.database.entity.CategoryEntity

// Schemas are exported to core/database/schemas. Every change needs a version bump and a
// migration; destructive migration is deliberately not enabled.
@Database(
    entities = [CategoryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LifeOSDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
}
