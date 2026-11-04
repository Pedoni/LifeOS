package it.edon.lifeos.feature.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.designsystem.theme.CategoryColors
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.model.CategoryType
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.domain.repository.CategoryRepository
import it.edon.lifeos.core.domain.repository.TaskRepository
import it.edon.lifeos.core.domain.task.CategoryNameIssue
import it.edon.lifeos.core.domain.task.CreateTaskCategoryUseCase
import it.edon.lifeos.core.domain.task.DurationIssue
import it.edon.lifeos.core.domain.task.SaveTaskUseCase
import it.edon.lifeos.core.domain.task.TaskFailure
import it.edon.lifeos.core.domain.task.TaskInput
import it.edon.lifeos.core.domain.task.TitleIssue
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DuePicker { DATE, TIME }

/** The small form for a category created while editing a task. */
data class NewCategoryDraft(
    val name: String = "",
    val colorHex: String = CategoryColors.first().hex,
    val issue: CategoryNameIssue? = null,
    val isSaving: Boolean = false
)

sealed interface TaskEditorUiState {
    /** Waiting for the task being edited. */
    data object Loading : TaskEditorUiState

    /** The task being edited does not exist (any more). */
    data object NotFound : TaskEditorUiState

    data class Form(
        val isNew: Boolean,
        val title: String = "",
        val description: String = "",
        val notes: String = "",
        val status: TaskStatus = TaskStatus.TODO,
        val priority: TaskPriority = TaskPriority.MEDIUM,
        val dueDate: LocalDate? = null,
        val dueTime: LocalTime? = null,
        val duration: String = "",
        val categoryId: String? = null,
        val categories: List<Category> = emptyList(),
        val newCategory: NewCategoryDraft? = null,
        val titleIssue: TitleIssue? = null,
        val durationIssue: DurationIssue? = null,
        val picker: DuePicker? = null,
        val isSaving: Boolean = false,
        val isDeleteDialogVisible: Boolean = false,
        /** The task was saved or deleted: the screen should close. */
        val isFinished: Boolean = false
    ) : TaskEditorUiState
}

sealed interface TaskEditorEvent {
    /** Changes what the person types or chooses in the form. */
    sealed interface FieldEvent : TaskEditorEvent

    /** Changes the due date, its time, or the picker that is open. */
    sealed interface DueEvent : TaskEditorEvent

    /** Chooses a category, or fills in the form that creates one. */
    sealed interface CategoryEvent : TaskEditorEvent

    data class TitleChanged(val value: String) : FieldEvent

    data class DescriptionChanged(val value: String) : FieldEvent

    data class NotesChanged(val value: String) : FieldEvent

    data class DurationChanged(val value: String) : FieldEvent

    data class StatusSelected(val status: TaskStatus) : FieldEvent

    data class PrioritySelected(val priority: TaskPriority) : FieldEvent

    data class ShowPicker(val picker: DuePicker) : DueEvent

    data object DismissPicker : DueEvent

    data class DateSelected(val date: LocalDate) : DueEvent

    data class TimeSelected(val time: LocalTime) : DueEvent

    data object ClearTime : DueEvent

    data object ClearDue : DueEvent

    data class CategorySelected(val categoryId: String?) : CategoryEvent

    data object ShowNewCategory : CategoryEvent

    data class NewCategoryNameChanged(val value: String) : CategoryEvent

    data class NewCategoryColorSelected(val colorHex: String) : CategoryEvent

    data object DismissNewCategory : CategoryEvent

    data object ConfirmNewCategory : TaskEditorEvent

    data object Save : TaskEditorEvent

    data object RequestDelete : TaskEditorEvent

    data object DismissDelete : TaskEditorEvent

    data object ConfirmDelete : TaskEditorEvent
}

@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val saveTask: SaveTaskUseCase,
    private val createCategory: CreateTaskCategoryUseCase
) : ViewModel() {
    // Navigation stores each route argument under the name of its property.
    private val taskId: String? = savedStateHandle.get<String?>(TaskEditorRoute::taskId.name)
    private val zone: ZoneId get() = ZoneId.systemDefault()

    private val _uiState = MutableStateFlow<TaskEditorUiState>(
        if (taskId == null) TaskEditorUiState.Form(isNew = true) else TaskEditorUiState.Loading
    )
    val uiState: StateFlow<TaskEditorUiState> = _uiState.asStateFlow()

    // Kept apart from the form so a task that loads later still gets the categories.
    private var taskCategories: List<Category> = emptyList()

    init {
        if (taskId != null) load(taskId)
        observeCategories()
    }

    fun onEvent(event: TaskEditorEvent) {
        when (event) {
            is TaskEditorEvent.FieldEvent -> edit { withField(event) }
            is TaskEditorEvent.DueEvent -> edit { withDue(event) }
            is TaskEditorEvent.CategoryEvent -> edit { withCategory(event) }
            TaskEditorEvent.ConfirmNewCategory -> createNewCategory()
            TaskEditorEvent.Save -> save()
            TaskEditorEvent.RequestDelete -> edit { copy(isDeleteDialogVisible = true) }
            TaskEditorEvent.DismissDelete -> edit { copy(isDeleteDialogVisible = false) }
            TaskEditorEvent.ConfirmDelete -> delete()
        }
    }

    private fun edit(change: TaskEditorUiState.Form.() -> TaskEditorUiState.Form) {
        _uiState.update { if (it is TaskEditorUiState.Form) it.change() else it }
    }

    private fun load(id: String) {
        viewModelScope.launch {
            val task = taskRepository.getTask(id)
            _uiState.value = task?.toForm(zone)?.copy(categories = taskCategories)
                ?: TaskEditorUiState.NotFound
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            val userId = currentUserId() ?: return@launch
            categoryRepository.observeCategories(userId).collect { all ->
                taskCategories = all.filter { it.type == CategoryType.TASK }
                edit { copy(categories = taskCategories) }
            }
        }
    }

    private fun createNewCategory() {
        val form = _uiState.value as? TaskEditorUiState.Form ?: return
        val draft = form.newCategory?.takeUnless { it.isSaving } ?: return
        edit { copy(newCategory = draft.copy(isSaving = true, issue = null)) }
        viewModelScope.launch {
            val userId = currentUserId()
            val outcome = userId?.let { createCategory(it, draft.name, draft.colorHex) }
            when (outcome) {
                is Outcome.Success ->
                    edit { copy(categoryId = outcome.value.id, newCategory = null) }

                is Outcome.Failure ->
                    edit { copy(newCategory = draft.copy(issue = outcome.error)) }

                null -> edit { copy(newCategory = draft) }
            }
        }
    }

    private fun save() {
        val form = _uiState.value as? TaskEditorUiState.Form ?: return
        if (form.isSaving) return
        edit { copy(isSaving = true) }
        viewModelScope.launch {
            val userId = currentUserId()
            if (userId == null) {
                edit { copy(isSaving = false) }
                return@launch
            }
            val input = TaskInput(
                id = taskId,
                title = form.title,
                description = form.description,
                notes = form.notes,
                status = form.status,
                priority = form.priority,
                due = form.dueDate?.let { DueSelection(it, form.dueTime) }.toDueDate(zone),
                categoryId = form.categoryId,
                estimatedDurationMinutes = form.duration
            )
            when (val outcome = saveTask(userId, input)) {
                is Outcome.Success -> edit { copy(isSaving = false, isFinished = true) }

                is Outcome.Failure -> when (val failure = outcome.error) {
                    is TaskFailure.InvalidInput -> edit {
                        copy(
                            isSaving = false,
                            titleIssue = failure.title,
                            durationIssue = failure.duration
                        )
                    }

                    TaskFailure.NotFound -> _uiState.value = TaskEditorUiState.NotFound
                }
            }
        }
    }

    private fun delete() {
        val id = taskId ?: return
        edit { copy(isDeleteDialogVisible = false, isSaving = true) }
        viewModelScope.launch {
            taskRepository.delete(id)
            edit { copy(isSaving = false, isFinished = true) }
        }
    }

    private suspend fun currentUserId(): String? = (
        authRepository.authState.first {
            it !is AuthState.Loading
        } as? AuthState.SignedIn
        )?.user?.id
}

private fun Task.toForm(zone: ZoneId): TaskEditorUiState.Form {
    val selection = due?.toSelection(zone)
    return TaskEditorUiState.Form(
        isNew = false,
        title = title,
        description = description.orEmpty(),
        notes = notes.orEmpty(),
        status = status,
        priority = priority,
        dueDate = selection?.date,
        dueTime = selection?.time,
        duration = estimatedDurationMinutes?.toString().orEmpty(),
        categoryId = categoryId
    )
}

private fun TaskEditorUiState.Form.withField(event: TaskEditorEvent.FieldEvent) = when (event) {
    is TaskEditorEvent.TitleChanged -> copy(title = event.value, titleIssue = null)
    is TaskEditorEvent.DescriptionChanged -> copy(description = event.value)
    is TaskEditorEvent.NotesChanged -> copy(notes = event.value)
    is TaskEditorEvent.DurationChanged -> copy(duration = event.value, durationIssue = null)
    is TaskEditorEvent.StatusSelected -> copy(status = event.status)
    is TaskEditorEvent.PrioritySelected -> copy(priority = event.priority)
}

private fun TaskEditorUiState.Form.withDue(event: TaskEditorEvent.DueEvent) = when (event) {
    is TaskEditorEvent.ShowPicker -> copy(picker = event.picker)
    TaskEditorEvent.DismissPicker -> copy(picker = null)
    is TaskEditorEvent.DateSelected -> copy(dueDate = event.date, picker = null)
    is TaskEditorEvent.TimeSelected -> copy(dueTime = event.time, picker = null)
    TaskEditorEvent.ClearTime -> copy(dueTime = null)
    TaskEditorEvent.ClearDue -> copy(dueDate = null, dueTime = null)
}

private fun TaskEditorUiState.Form.withCategory(event: TaskEditorEvent.CategoryEvent) =
    when (event) {
        is TaskEditorEvent.CategorySelected -> copy(categoryId = event.categoryId)

        TaskEditorEvent.ShowNewCategory -> copy(newCategory = NewCategoryDraft())

        is TaskEditorEvent.NewCategoryNameChanged ->
            copy(newCategory = newCategory?.copy(name = event.value, issue = null))

        is TaskEditorEvent.NewCategoryColorSelected ->
            copy(newCategory = newCategory?.copy(colorHex = event.colorHex))

        TaskEditorEvent.DismissNewCategory -> copy(newCategory = null)
    }
