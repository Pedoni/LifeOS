package it.edon.lifeos.feature.tasks

import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.edon.lifeos.core.designsystem.component.LifeOSButton
import it.edon.lifeos.core.designsystem.component.LifeOSChoiceChip
import it.edon.lifeos.core.designsystem.component.LifeOSConfirmDialog
import it.edon.lifeos.core.designsystem.component.LifeOSDatePickerDialog
import it.edon.lifeos.core.designsystem.component.LifeOSEmptyState
import it.edon.lifeos.core.designsystem.component.LifeOSLoadingState
import it.edon.lifeos.core.designsystem.component.LifeOSSwitchRow
import it.edon.lifeos.core.designsystem.component.LifeOSTextButton
import it.edon.lifeos.core.designsystem.component.LifeOSTextField
import it.edon.lifeos.core.designsystem.component.LifeOSTimePickerDialog
import it.edon.lifeos.core.designsystem.component.LifeOSTopBar
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import it.edon.lifeos.core.domain.task.DurationIssue
import it.edon.lifeos.core.domain.task.TitleIssue
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun TextFields(form: TaskEditorUiState.Form, onEvent: (TaskEditorEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.md)) {
        LifeOSTextField(
            value = form.title,
            onValueChange = { onEvent(TaskEditorEvent.TitleChanged(it)) },
            label = stringResource(R.string.task_editor_field_title),
            enabled = !form.isSaving,
            isError = form.titleIssue != null,
            supportingText = form.titleIssue?.message(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        LifeOSTextField(
            value = form.description,
            onValueChange = { onEvent(TaskEditorEvent.DescriptionChanged(it)) },
            label = stringResource(R.string.task_editor_field_description),
            enabled = !form.isSaving,
            singleLine = false,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        LifeOSTextField(
            value = form.notes,
            onValueChange = { onEvent(TaskEditorEvent.NotesChanged(it)) },
            label = stringResource(R.string.task_editor_field_notes),
            enabled = !form.isSaving,
            singleLine = false,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        LifeOSTextField(
            value = form.duration,
            onValueChange = { onEvent(TaskEditorEvent.DurationChanged(it)) },
            label = stringResource(R.string.task_editor_field_duration),
            enabled = !form.isSaving,
            isError = form.durationIssue != null,
            supportingText = form.durationIssue?.message(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PrioritySection(form: TaskEditorUiState.Form, onEvent: (TaskEditorEvent) -> Unit) {
    Section(title = stringResource(R.string.task_editor_section_priority)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.sm)) {
            TaskPriority.entries.forEach { priority ->
                LifeOSChoiceChip(
                    label = stringResource(priority.labelRes()),
                    selected = form.priority == priority,
                    onClick = { onEvent(TaskEditorEvent.PrioritySelected(priority)) },
                    enabled = !form.isSaving
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StatusSection(form: TaskEditorUiState.Form, onEvent: (TaskEditorEvent) -> Unit) {
    Section(title = stringResource(R.string.task_editor_section_status)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.sm)) {
            TaskStatus.entries.forEach { status ->
                LifeOSChoiceChip(
                    label = stringResource(status.labelRes()),
                    selected = form.status == status,
                    onClick = { onEvent(TaskEditorEvent.StatusSelected(status)) },
                    enabled = !form.isSaving
                )
            }
        }
    }
}

@Composable
internal fun DueSection(form: TaskEditorUiState.Form, onEvent: (TaskEditorEvent) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val zone = ZoneId.systemDefault()
    Section(title = stringResource(R.string.task_editor_section_due)) {
        val date = form.dueDate
        val dateLabel = date?.let { DueDate.AllDay(it).format(zone, locale) }
            ?: stringResource(R.string.task_editor_due_none)
        LifeOSTextButton(
            text = dateLabel,
            onClick = { onEvent(TaskEditorEvent.ShowPicker(DuePicker.DATE)) },
            enabled = !form.isSaving
        )
        if (date != null) {
            LifeOSSwitchRow(
                label = stringResource(R.string.task_editor_due_time),
                supportingText = stringResource(R.string.task_editor_due_time_description),
                checked = form.dueTime != null,
                onCheckedChange = { enabled ->
                    onEvent(
                        if (enabled) {
                            TaskEditorEvent.ShowPicker(DuePicker.TIME)
                        } else {
                            TaskEditorEvent.ClearTime
                        }
                    )
                },
                enabled = !form.isSaving
            )
            form.dueTime?.let { time ->
                LifeOSTextButton(
                    text = time.format(
                        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
                    ),
                    onClick = { onEvent(TaskEditorEvent.ShowPicker(DuePicker.TIME)) },
                    enabled = !form.isSaving
                )
            }
            LifeOSTextButton(
                text = stringResource(R.string.task_editor_due_remove),
                onClick = { onEvent(TaskEditorEvent.ClearDue) },
                enabled = !form.isSaving
            )
        }
    }
}

@Composable
internal fun Dialogs(form: TaskEditorUiState.Form, onEvent: (TaskEditorEvent) -> Unit) {
    form.newCategory?.let { NewCategoryDialog(draft = it, onEvent = onEvent) }
    val ok = stringResource(R.string.task_editor_picker_ok)
    val cancel = stringResource(R.string.task_editor_cancel)
    when (form.picker) {
        DuePicker.DATE -> LifeOSDatePickerDialog(
            initialDate = form.dueDate,
            confirmLabel = ok,
            dismissLabel = cancel,
            onConfirm = { onEvent(TaskEditorEvent.DateSelected(it)) },
            onDismiss = { onEvent(TaskEditorEvent.DismissPicker) }
        )

        DuePicker.TIME -> LifeOSTimePickerDialog(
            initialTime = form.dueTime ?: LocalTime.NOON,
            is24Hour = DateFormat.is24HourFormat(LocalContext.current),
            confirmLabel = ok,
            dismissLabel = cancel,
            onConfirm = { onEvent(TaskEditorEvent.TimeSelected(it)) },
            onDismiss = { onEvent(TaskEditorEvent.DismissPicker) }
        )

        null -> Unit
    }
    if (form.isDeleteDialogVisible) {
        LifeOSConfirmDialog(
            title = stringResource(R.string.task_editor_delete_title),
            message = stringResource(R.string.task_editor_delete_message),
            confirmLabel = stringResource(R.string.task_editor_delete_confirm),
            dismissLabel = cancel,
            onConfirm = { onEvent(TaskEditorEvent.ConfirmDelete) },
            onDismiss = { onEvent(TaskEditorEvent.DismissDelete) },
            isDestructive = true,
            isBusy = form.isSaving
        )
    }
}

@Composable
internal fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.sm)) {
        Text(
            text = title,
            style = LifeOSTheme.typography.titleMedium,
            color = LifeOSTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        content()
    }
}

@Composable
private fun TitleIssue.message(): String = when (this) {
    TitleIssue.BLANK -> stringResource(R.string.task_editor_issue_title_blank)

    TitleIssue.TOO_LONG ->
        stringResource(R.string.task_editor_issue_title_too_long, Task.MAX_TITLE_LENGTH)
}

@Composable
private fun DurationIssue.message(): String = when (this) {
    DurationIssue.NOT_A_NUMBER -> stringResource(R.string.task_editor_issue_duration_not_a_number)
    DurationIssue.NOT_POSITIVE -> stringResource(R.string.task_editor_issue_duration_not_positive)
}
