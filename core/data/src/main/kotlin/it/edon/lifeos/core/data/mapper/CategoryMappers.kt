package it.edon.lifeos.core.data.mapper

import it.edon.lifeos.core.database.entity.CategoryEntity
import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.model.CategoryType
import java.time.Instant

internal fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    userId = userId,
    name = name,
    color = color,
    type = CategoryType.valueOf(type),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt)
)
