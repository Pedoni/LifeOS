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
fun TaskEditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Saving and deleting leave the screen, but the screen decides that, not the view model.
    LaunchedEffect((state as? TaskEditorUiState.Form)?.isFinished) {
        if ((state as? TaskEditorUiState.Form)?.isFinished == true) onBack()
    }
    TaskEditorContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
internal fun TaskEditorContent(
    state: TaskEditorUiState,
    onEvent: (TaskEditorEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNew = (state as? TaskEditorUiState.Form)?.isNew ?: false
    Scaffold(
        modifier = modifier,
        topBar = {
            LifeOSTopBar(
                title = stringResource(
                    if (isNew) R.string.task_editor_title_new else R.string.task_editor_title_edit
                ),
                onBack = onBack,
                backDescription = stringResource(R.string.task_editor_back)
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (state) {
            TaskEditorUiState.Loading -> LifeOSLoadingState(
                modifier = contentModifier,
                message = stringResource(R.string.task_editor_loading)
            )

            TaskEditorUiState.NotFound -> LifeOSEmptyState(
                title = stringResource(R.string.task_editor_not_found_title),
                message = stringResource(R.string.task_editor_not_found_message),
                modifier = contentModifier,
                actionLabel = stringResource(R.string.task_editor_back_to_list),
                onAction = onBack
            )

            is TaskEditorUiState.Form -> FormContent(state, onEvent, contentModifier)
        }
    }
}

@Composable
private fun FormContent(
    form: TaskEditorUiState.Form,
    onEvent: (TaskEditorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LifeOSTheme.spacing.xl),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.widthIn(max = LifeOSTheme.spacing.formMaxWidth),
            verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.xl)
        ) {
            TextFields(form, onEvent)
            PrioritySection(form, onEvent)
            CategorySection(form, onEvent)
            StatusSection(form, onEvent)
            DueSection(form, onEvent)
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.sm)) {
                LifeOSButton(
                    text = stringResource(R.string.task_editor_save),
                    onClick = { onEvent(TaskEditorEvent.Save) },
                    isLoading = form.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!form.isNew) {
                    LifeOSTextButton(
                        text = stringResource(R.string.task_editor_delete),
                        onClick = { onEvent(TaskEditorEvent.RequestDelete) },
                        enabled = !form.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    Dialogs(form, onEvent)
}
