package it.edon.lifeos.core.domain.task

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskStatus
import it.edon.lifeos.core.domain.repository.TaskRepository
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * Creates a task or applies an edit to an existing one, after checking the rules. What the person
 * cannot change here (owner, creation time, category) is kept from the stored task.
 */
class SaveTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(userId: String, input: TaskInput): Outcome<Task, TaskFailure> {
        val prepared = prepare(userId, input)
        if (prepared is Outcome.Success) repository.save(prepared.value)
        return prepared
    }

    private suspend fun prepare(userId: String, input: TaskInput): Outcome<Task, TaskFailure> {
        val titleIssue = titleIssue(input.title)
        val duration = parseDuration(input.estimatedDurationMinutes)
        if (titleIssue != null || duration is Duration.Invalid) {
            return Outcome.Failure(
                TaskFailure.InvalidInput(
                    title = titleIssue,
                    duration = (duration as? Duration.Invalid)?.issue
                )
            )
        }

        val existing = input.id?.let { repository.getTask(it) }
        val minutes = (duration as Duration.Valid).minutes
        return if (input.id != null && existing == null) {
            Outcome.Failure(TaskFailure.NotFound)
        } else {
            Outcome.Success(build(userId, input, existing, minutes))
        }
    }

    private fun build(userId: String, input: TaskInput, existing: Task?, minutes: Int?): Task {
        val now = clock.instant()
        return Task(
            id = existing?.id ?: UUID.randomUUID().toString(),
            userId = userId,
            title = input.title.trim(),
            description = input.description.trim().ifEmpty { null },
            notes = input.notes.trim().ifEmpty { null },
            status = input.status,
            priority = input.priority,
            due = input.due,
            estimatedDurationMinutes = minutes,
            categoryId = existing?.categoryId,
            completedAt = completionTime(input.status, existing, now),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
    }

    // An earlier completion time is kept when the task stays completed.
    private fun completionTime(status: TaskStatus, existing: Task?, now: Instant): Instant? =
        if (status == TaskStatus.COMPLETED) existing?.completedAt ?: now else null

    companion object {
        fun titleIssue(title: String): TitleIssue? = when {
            title.isBlank() -> TitleIssue.BLANK
            title.trim().length > Task.MAX_TITLE_LENGTH -> TitleIssue.TOO_LONG
            else -> null
        }

        private fun parseDuration(text: String): Duration {
            val minutes = text.trim().toIntOrNull()
            return when {
                text.isBlank() -> Duration.Valid(null)
                minutes == null -> Duration.Invalid(DurationIssue.NOT_A_NUMBER)
                minutes <= 0 -> Duration.Invalid(DurationIssue.NOT_POSITIVE)
                else -> Duration.Valid(minutes)
            }
        }
    }

    private sealed interface Duration {
        data class Valid(val minutes: Int?) : Duration

        data class Invalid(val issue: DurationIssue) : Duration
    }
}
