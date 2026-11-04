package it.edon.lifeos.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.edon.lifeos.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query(
        "SELECT * FROM categories " +
            "WHERE userId = :userId AND deletedAt IS NULL " +
            "ORDER BY name COLLATE NOCASE"
    )
    fun observeActive(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Upsert
    suspend fun upsert(entity: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
