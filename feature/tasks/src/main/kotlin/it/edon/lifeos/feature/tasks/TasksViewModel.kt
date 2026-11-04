package it.edon.lifeos.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.domain.repository.TaskRepository
import it.edon.lifeos.core.domain.task.SetTaskCompletedUseCase
import java.time.Clock
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** A task as the list shows it. */
data class TaskRow(val task: Task, val isOverdue: Boolean)

sealed interface TasksUiState {
    data object Loading : TasksUiState

    data object Empty : TasksUiState

    data class Content(val rows: List<TaskRow>) : TasksUiState

    /** The tasks could not be read; the screen offers to try again. */
    data object Error : TasksUiState
}

sealed interface TasksEvent {
    data class SetCompleted(val taskId: String, val completed: Boolean) : TasksEvent

    data object Retry : TasksEvent
}

@HiltViewModel
class TasksViewModel @Inject constructor(
    authRepository: AuthRepository,
    taskRepository: TaskRepository,
    private val setTaskCompleted: SetTaskCompletedUseCase,
    private val clock: Clock
) : ViewModel() {
    // Bumped to subscribe again after a failure.
    private val attempt = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TasksUiState> = combine(authRepository.authState, attempt) { auth, _ ->
        (auth as? AuthState.SignedIn)?.user?.id
    }
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(TasksUiState.Loading)
            } else {
                taskRepository.observeTasks(userId)
                    .map(::toUiState)
                    // The way out of a failed read is the retry button of the error state.
                    .catch { emit(TasksUiState.Error) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = TasksUiState.Loading
        )

    fun onEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.SetCompleted -> viewModelScope.launch {
                // A task that disappeared meanwhile is already gone from the list: nothing to report.
                setTaskCompleted(event.taskId, event.completed)
            }

            TasksEvent.Retry -> attempt.update { it + 1 }
        }
    }

    private fun toUiState(tasks: List<Task>): TasksUiState {
        if (tasks.isEmpty()) return TasksUiState.Empty
        val now = clock.instant()
        val zone = ZoneId.systemDefault()
        return TasksUiState.Content(tasks.map { TaskRow(it, it.isOverdue(now, zone)) })
    }
}
