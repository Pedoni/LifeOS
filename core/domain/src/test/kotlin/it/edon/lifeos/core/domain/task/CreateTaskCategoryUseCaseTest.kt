package it.edon.lifeos.core.domain.task

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.model.CategoryType
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateTaskCategoryUseCaseTest {
    private val now = Instant.parse("2026-03-10T09:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    private fun existing(
        name: String,
        type: CategoryType = CategoryType.TASK,
        userId: String = "user"
    ) = Category("c-$name-$type-$userId", userId, name, "#3F51B5", type, now, now)

    @Test
    fun createsATaskCategoryWithTrimmedName() = runTest {
        val repository = FakeCategoryRepository()

        val outcome = CreateTaskCategoryUseCase(repository, clock)("user", "  Home ", "#009688")

        val category = (outcome as Outcome.Success).value
        assertEquals("Home", category.name)
        assertEquals("#009688", category.color)
        assertEquals(CategoryType.TASK, category.type)
        assertEquals("user", category.userId)
        assertEquals(category, repository.categories.value.getValue(category.id))
    }

    @Test
    fun blankAndTooLongNamesAreRejected() = runTest {
        val useCase = CreateTaskCategoryUseCase(FakeCategoryRepository(), clock)

        assertEquals(Outcome.Failure(CategoryNameIssue.BLANK), useCase("user", "   ", "#009688"))
        assertEquals(
            Outcome.Failure(CategoryNameIssue.TOO_LONG),
            useCase("user", "x".repeat(Category.MAX_NAME_LENGTH + 1), "#009688")
        )
    }

    @Test
    fun aNameCannotRepeatIgnoringCase() = runTest {
        val repository = FakeCategoryRepository(existing("Home"))

        val outcome = CreateTaskCategoryUseCase(repository, clock)("user", " home ", "#009688")

        assertEquals(Outcome.Failure(CategoryNameIssue.ALREADY_EXISTS), outcome)
        assertEquals(1, repository.categories.value.size)
    }

    @Test
    fun otherTypesAndOtherUsersDoNotCount() = runTest {
        val repository = FakeCategoryRepository(
            existing("Home", type = CategoryType.GOAL),
            existing("Home", userId = "someone-else")
        )

        val outcome = CreateTaskCategoryUseCase(repository, clock)("user", "Home", "#009688")

        assertTrue(outcome is Outcome.Success)
    }
}
