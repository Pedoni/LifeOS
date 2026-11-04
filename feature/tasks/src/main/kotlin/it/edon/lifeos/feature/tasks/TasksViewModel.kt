package it.edon.lifeos.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.model.CategoryType
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.domain.repository.CategoryRepository
import it.edon.lifeos.core.domain.repository.TaskRepository
import it.edon.lifeos.core.domain.task.SetTaskCompletedUseCase
import it.edon.lifeos.core.domain.task.SortDirection
import it.edon.lifeos.core.domain.task.TaskFilter
import it.edon.lifeos.core.domain.task.TaskListQuery
import it.edon.lifeos.core.domain.task.TaskSortField
import it.edon.lifeos.core.domain.task.TaskView
import java.time.Clock
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** A task as the list shows it. */
data class TaskRow(val task: Task, val isOverdue: Boolean, val category: Category? = null)

/** Rows that share a priority; [priority] is null when the list is not grouped. */
data class TaskRowGroup(val priority: TaskPriority?, val rows: List<TaskRow>)

sealed interface TasksUiState {
    data object Loading : TasksUiState

    /** There are no tasks at all, whatever the view: the person is invited to add the first. */
    data object Empty : TasksUiState

    /** [groups] can be empty when the chosen view and filter match nothing. */
    data class Content(
        val query: TaskListQuery,
        val groups: List<TaskRowGroup>,
        val categories: List<Category>,
        val isOptionsVisible: Boolean = false
    ) : TasksUiState

    /** The tasks could not be read; the screen offers to try again. */
    data object Error : TasksUiState
}

sealed interface TasksEvent {
    /** Changes what the list shows or how it is ordered. */
    sealed interface QueryEvent : TasksEvent

    data class ViewSelected(val view: TaskView) : QueryEvent

    data class SortFieldSelected(val field: TaskSortField) : QueryEvent

    data class SortDirectionSelected(val direction: SortDirection) : QueryEvent

    data class PriorityToggled(val priority: TaskPriority) : QueryEvent

    data class CategoryFilterSelected(val categoryId: String?) : QueryEvent

    data class GroupByPriorityChanged(val enabled: Boolean) : QueryEvent

    data object ResetQuery : QueryEvent

    data object ShowOptions : TasksEvent

    data object DismissOptions : TasksEvent

    data class SetCompleted(val taskId: String, val completed: Boolean) : TasksEvent

    data object Retry : TasksEvent
}

@HiltViewModel
class TasksViewModel @Inject constructor(
    authRepository: AuthRepository,
    taskRepository: TaskRepository,
    categoryRepository: CategoryRepository,
    private val setTaskCompleted: SetTaskCompletedUseCase,
    private val clock: Clock
) : ViewModel() {
    // Bumped to subscribe again after a failure.
    private val attempt = MutableStateFlow(0)

    // What the person chose is kept only while the app lives, on purpose (decision D-25).
    private val query = MutableStateFlow(TaskListQuery())
    private val isOptionsVisible = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val data: Flow<TasksData> = combine(authRepository.authState, attempt) { auth, _ ->
        (auth as? AuthState.SignedIn)?.user?.id
    }
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(TasksData.Loading)
            } else {
                val loaded: Flow<TasksData> = combine(
                    taskRepository.observeTasks(userId),
                    categoryRepository.observeCategories(userId)
                ) { tasks, categories ->
                    TasksData.Loaded(tasks, categories.filter { it.type == CategoryType.TASK })
                }
                // The way out of a failed read is the retry button of the error state.
                loaded.catch { emit(TasksData.Failed) }
            }
        }

    val uiState: StateFlow<TasksUiState> = combine(data, query, isOptionsVisible) {
            data,
            query,
            options
        ->
        when (data) {
            TasksData.Loading -> TasksUiState.Loading

            TasksData.Failed -> TasksUiState.Error

            is TasksData.Loaded ->
                if (data.tasks.isEmpty()) TasksUiState.Empty else content(data, query, options)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = TasksUiState.Loading
    )

    fun onEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.QueryEvent -> query.update { it.changedBy(event) }

            TasksEvent.ShowOptions -> isOptionsVisible.value = true

            TasksEvent.DismissOptions -> isOptionsVisible.value = false

            is TasksEvent.SetCompleted -> viewModelScope.launch {
                // A task that disappeared meanwhile is already gone from the list: nothing to report.
                setTaskCompleted(event.taskId, event.completed)
            }

            TasksEvent.Retry -> attempt.update { it + 1 }
        }
    }

    private fun content(
        data: TasksData.Loaded,
        query: TaskListQuery,
        options: Boolean
    ): TasksUiState {
        val now = clock.instant()
        val zone = ZoneId.systemDefault()
        val categories = data.categories.associateBy { it.id }
        val groups = query.apply(data.tasks, now, zone).map { group ->
            TaskRowGroup(
                priority = group.priority,
                rows = group.tasks.map { task ->
                    TaskRow(task, task.isOverdue(now, zone), task.categoryId?.let(categories::get))
                }
            )
        }
        return TasksUiState.Content(query, groups, data.categories, options)
    }
}

private sealed interface TasksData {
    data object Loading : TasksData

    data object Failed : TasksData

    data class Loaded(val tasks: List<Task>, val categories: List<Category>) : TasksData
}

private fun TaskListQuery.changedBy(event: TasksEvent.QueryEvent): TaskListQuery = when (event) {
    is TasksEvent.ViewSelected -> copy(view = event.view)

    is TasksEvent.SortFieldSelected -> copy(sort = sort.copy(field = event.field))

    is TasksEvent.SortDirectionSelected -> copy(sort = sort.copy(direction = event.direction))

    is TasksEvent.PriorityToggled -> copy(filter = filter.toggled(event.priority))

    is TasksEvent.CategoryFilterSelected -> copy(
        filter = filter.copy(categoryId = event.categoryId)
    )

    is TasksEvent.GroupByPriorityChanged -> copy(groupByPriority = event.enabled)

    TasksEvent.ResetQuery -> TaskListQuery()
}

private fun TaskFilter.toggled(priority: TaskPriority) = copy(
    priorities = if (priority in priorities) priorities - priority else priorities + priority
)
