package it.edon.lifeos.core.domain.repository

import it.edon.lifeos.core.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    /** Categories of [userId] that have not been deleted, ordered by name. */
    fun observeCategories(userId: String): Flow<List<Category>>

    /** Creates the category, or updates it when the id already exists. */
    suspend fun save(category: Category)

    suspend fun delete(id: String)
}
