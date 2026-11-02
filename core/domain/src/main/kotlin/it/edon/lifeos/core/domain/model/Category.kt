package it.edon.lifeos.core.domain.model

import java.time.Instant

/** What a category can be attached to. */
enum class CategoryType { TASK, GOAL, EVENT }

/**
 * A user-defined label with a color. The rules mirror the backend constraints, so a category
 * that exists in the domain can always be synchronized.
 */
data class Category(
    val id: String,
    val userId: String,
    val name: String,
    val color: String?,
    val type: CategoryType,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank() && name.trim().length <= MAX_NAME_LENGTH) {
            "Category name must have between 1 and $MAX_NAME_LENGTH characters"
        }
        require(color == null || COLOR_PATTERN.matches(color)) {
            "Category color must be #RRGGBB or #RRGGBBAA"
        }
    }

    companion object {
        const val MAX_NAME_LENGTH = 100
        private val COLOR_PATTERN = Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$")
    }
}
