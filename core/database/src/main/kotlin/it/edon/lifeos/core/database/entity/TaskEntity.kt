package it.edon.lifeos.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import it.edon.lifeos.core.database.SyncStatus

/**
 * Same conventions as [CategoryEntity]. Times are epoch milliseconds. When [dueAllDay] is true,
 * [dueDate] is midnight UTC of the due day, which matches how the backend stores it.
 */
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["userId", "status"]), Index(value = ["userId", "dueDate"])]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val notes: String?,
    val status: String,
    val priority: String,
    val dueDate: Long?,
    val dueAllDay: Boolean,
    val estimatedDurationMinutes: Int?,
    val categoryId: String?,
    val completedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val syncStatus: SyncStatus,
    val remoteUpdatedAt: String?
)
