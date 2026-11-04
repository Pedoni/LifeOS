package it.edon.lifeos.core.domain.task

import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeCategoryRepository(vararg initial: Category) : CategoryRepository {
    val categories = MutableStateFlow(initial.associateBy { it.id })

    override fun observeCategories(userId: String): Flow<List<Category>> =
        categories.map { map -> map.values.filter { it.userId == userId } }

    override suspend fun save(category: Category) {
        categories.value = categories.value + (category.id to category)
    }

    override suspend fun delete(id: String) {
        categories.value = categories.value - id
    }
}
