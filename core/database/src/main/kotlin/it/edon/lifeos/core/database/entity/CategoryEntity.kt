package it.edon.lifeos.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import it.edon.lifeos.core.database.SyncStatus

/**
 * Reference entity for the synchronized tables: every one of them carries the owner, the
 * timestamps, a tombstone and the local sync state.
 *
 * Times are epoch milliseconds. [remoteUpdatedAt] is the opaque `updated_at` last returned by
 * the server, kept as text so the microseconds survive; it is sent back as `base_updated_at`
 * on the next push and is null for rows that were never synchronized.
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["userId", "type"])]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val color: String?,
    val type: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val syncStatus: SyncStatus,
    val remoteUpdatedAt: String?
)
