package it.edon.lifeos.core.domain.task

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.model.CategoryType
import it.edon.lifeos.core.domain.repository.CategoryRepository
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.first

enum class CategoryNameIssue { BLANK, TOO_LONG, ALREADY_EXISTS }

/**
 * Creates a category for tasks. A name cannot repeat among the person's task categories, ignoring
 * case: the backend refuses duplicates, and finding out only when synchronizing would be late.
 */
class CreateTaskCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        userId: String,
        name: String,
        color: String
    ): Outcome<Category, CategoryNameIssue> {
        val trimmed = name.trim()
        val issue = when {
            trimmed.isEmpty() -> CategoryNameIssue.BLANK

            trimmed.length > Category.MAX_NAME_LENGTH -> CategoryNameIssue.TOO_LONG

            repository.observeCategories(userId).first().any {
                it.type == CategoryType.TASK && it.name.trim().equals(trimmed, ignoreCase = true)
            } -> CategoryNameIssue.ALREADY_EXISTS

            else -> null
        }
        if (issue != null) return Outcome.Failure(issue)

        val now = clock.instant()
        val category = Category(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = trimmed,
            color = color,
            type = CategoryType.TASK,
            createdAt = now,
            updatedAt = now
        )
        repository.save(category)
        return Outcome.Success(category)
    }
}
