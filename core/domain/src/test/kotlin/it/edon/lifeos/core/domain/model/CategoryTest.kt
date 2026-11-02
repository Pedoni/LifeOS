package it.edon.lifeos.core.domain.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CategoryTest {
    private val now = Instant.parse("2026-01-01T00:00:00Z")

    private fun category(name: String = "Work", color: String? = "#3F51B5") = Category(
        id = "id",
        userId = "user",
        name = name,
        color = color,
        type = CategoryType.TASK,
        createdAt = now,
        updatedAt = now
    )

    @Test
    fun acceptsValidCategory() {
        assertEquals("Work", category().name)
    }

    @Test
    fun acceptsMissingColor() {
        assertEquals(null, category(color = null).color)
    }

    @Test
    fun acceptsColorWithAlpha() {
        assertEquals("#3F51B5CC", category(color = "#3F51B5CC").color)
    }

    @Test
    fun rejectsBlankName() {
        assertThrows(IllegalArgumentException::class.java) { category(name = "   ") }
    }

    @Test
    fun rejectsTooLongName() {
        assertThrows(IllegalArgumentException::class.java) {
            category(name = "a".repeat(Category.MAX_NAME_LENGTH + 1))
        }
    }

    @Test
    fun rejectsMalformedColor() {
        assertThrows(IllegalArgumentException::class.java) { category(color = "blue") }
    }
}
