package it.edon.lifeos.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.edon.lifeos.core.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Open tasks first, by due date with the undated ones last; then the finished ones, the most
    // recently created first.
    @Query(
        "SELECT * FROM tasks " +
            "WHERE userId = :userId AND deletedAt IS NULL " +
            "ORDER BY status IN ('COMPLETED', 'CANCELLED'), dueDate IS NULL, dueDate, createdAt DESC"
    )
    fun observeActive(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Upsert
    suspend fun upsert(entity: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
