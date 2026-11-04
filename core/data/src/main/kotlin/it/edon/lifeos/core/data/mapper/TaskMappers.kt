package it.edon.lifeos.core.data.mapper

import it.edon.lifeos.core.database.entity.TaskEntity
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

internal fun TaskEntity.toDomain(): Task = Task(
    id = id,
    userId = userId,
    title = title,
    description = description,
    notes = notes,
    status = TaskStatus.valueOf(status),
    priority = TaskPriority.valueOf(priority),
    due = dueDate?.let { millis ->
        val instant = Instant.ofEpochMilli(millis)
        if (dueAllDay) {
            DueDate.AllDay(
                instant.atZone(ZoneOffset.UTC).toLocalDate()
            )
        } else {
            DueDate.At(instant)
        }
    },
    estimatedDurationMinutes = estimatedDurationMinutes,
    categoryId = categoryId,
    completedAt = completedAt?.let(Instant::ofEpochMilli),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt)
)

/** A day is stored as midnight UTC, so it does not move when the time zone changes. */
internal fun DueDate.toEpochMillis(): Long = when (this) {
    is DueDate.AllDay -> date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    is DueDate.At -> instant.toEpochMilli()
}
