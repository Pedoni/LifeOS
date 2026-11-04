package it.edon.lifeos.core.domain.task

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.TaskStatus
import it.edon.lifeos.core.domain.repository.TaskRepository
import java.time.Clock
import javax.inject.Inject

/** Completes a task, or reopens it as to do. Completing a completed task changes nothing. */
class SetTaskCompletedUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(taskId: String, completed: Boolean): Outcome<Unit, TaskFailure> {
        val task = repository.getTask(taskId) ?: return Outcome.Failure(TaskFailure.NotFound)
        val now = clock.instant()
        if (completed != (task.status == TaskStatus.COMPLETED)) {
            repository.save(
                if (completed) {
                    task.copy(status = TaskStatus.COMPLETED, completedAt = now, updatedAt = now)
                } else {
                    task.copy(status = TaskStatus.TODO, completedAt = null, updatedAt = now)
                }
            )
        }
        return Outcome.Success(Unit)
    }
}
