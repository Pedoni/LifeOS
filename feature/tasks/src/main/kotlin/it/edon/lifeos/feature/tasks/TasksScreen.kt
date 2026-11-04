package it.edon.lifeos.feature.tasks

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.edon.lifeos.core.designsystem.component.LifeOSAddButton
import it.edon.lifeos.core.designsystem.component.LifeOSEmptyState
import it.edon.lifeos.core.designsystem.component.LifeOSErrorState
import it.edon.lifeos.core.designsystem.component.LifeOSLoadingState
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

@Composable
fun TasksScreen(
    onAddTask: () -> Unit,
    onOpenTask: (taskId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TasksContent(
        state = state,
        onEvent = viewModel::onEvent,
        onAddTask = onAddTask,
        onOpenTask = onOpenTask,
        modifier = modifier
    )
}

@Composable
internal fun TasksContent(
    state: TasksUiState,
    onEvent: (TasksEvent) -> Unit,
    onAddTask: () -> Unit,
    onOpenTask: (taskId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            TasksUiState.Loading ->
                LifeOSLoadingState(message = stringResource(R.string.tasks_loading))

            TasksUiState.Empty -> LifeOSEmptyState(
                title = stringResource(R.string.tasks_empty_title),
                message = stringResource(R.string.tasks_empty_message),
                actionLabel = stringResource(R.string.tasks_add),
                onAction = onAddTask
            )

            TasksUiState.Error -> LifeOSErrorState(
                title = stringResource(R.string.tasks_error_title),
                message = stringResource(R.string.tasks_error_message),
                retryLabel = stringResource(R.string.tasks_retry),
                onRetry = { onEvent(TasksEvent.Retry) }
            )

            is TasksUiState.Content -> {
                TaskList(rows = state.rows, onEvent = onEvent, onOpenTask = onOpenTask)
                LifeOSAddButton(
                    label = stringResource(R.string.tasks_add),
                    onClick = onAddTask,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(LifeOSTheme.spacing.lg)
                )
            }
        }
    }
}

@Composable
private fun TaskList(
    rows: List<TaskRow>,
    onEvent: (TasksEvent) -> Unit,
    onOpenTask: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // The bottom space keeps the last task clear of the add button.
        contentPadding = PaddingValues(
            start = LifeOSTheme.spacing.lg,
            end = LifeOSTheme.spacing.lg,
            top = LifeOSTheme.spacing.lg,
            bottom = LifeOSTheme.spacing.xxxl * 2
        ),
        verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.xs)
    ) {
        item {
            Text(
                text = stringResource(R.string.tasks_title),
                style = LifeOSTheme.typography.headlineSmall,
                color = LifeOSTheme.colors.onSurface,
                modifier = Modifier
                    .padding(bottom = LifeOSTheme.spacing.sm)
                    .semantics { heading() }
            )
        }
        items(items = rows, key = { it.task.id }) { row ->
            TaskItem(
                row = row,
                onToggle = { onEvent(TasksEvent.SetCompleted(row.task.id, it)) },
                onOpen = { onOpenTask(row.task.id) }
            )
        }
    }
}

@Composable
private fun TaskItem(row: TaskRow, onToggle: (Boolean) -> Unit, onOpen: () -> Unit) {
    val task = row.task
    val isCompleted = task.status == TaskStatus.COMPLETED
    val toggleDescription = stringResource(
        if (isCompleted) R.string.tasks_mark_not_done else R.string.tasks_mark_done,
        task.title
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = LifeOSTheme.spacing.minTouchTarget),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isCompleted,
            onCheckedChange = onToggle,
            modifier = Modifier.semantics { contentDescription = toggleDescription }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpen)
                .heightIn(min = LifeOSTheme.spacing.minTouchTarget)
                .padding(vertical = LifeOSTheme.spacing.sm),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = task.title,
                style = LifeOSTheme.typography.bodyLarge,
                color = if (task.status.isOpen) {
                    LifeOSTheme.colors.onSurface
                } else {
                    LifeOSTheme.colors.onSurfaceVariant
                },
                textDecoration = if (task.status.isOpen) null else TextDecoration.LineThrough
            )
            TaskDetails(row)
        }
    }
}

// Priority is always written out: color alone would not reach everyone.
@Composable
private fun TaskDetails(row: TaskRow) {
    val task = row.task
    val locale = currentLocale()
    val zone = ZoneId.systemDefault()
    val parts = buildList {
        add(stringResource(task.priority.labelRes()))
        task.due?.let { add(stringResource(R.string.tasks_due, it.format(zone, locale))) }
        task.estimatedDurationMinutes?.let {
            add(stringResource(R.string.tasks_duration_minutes, it))
        }
    }
    Text(
        text = parts.joinToString(separator = " · "),
        style = LifeOSTheme.typography.bodyMedium,
        color = LifeOSTheme.colors.onSurfaceVariant
    )
    if (row.isOverdue) {
        Text(
            text = stringResource(R.string.tasks_overdue),
            style = LifeOSTheme.typography.labelMedium,
            color = LifeOSTheme.colors.error
        )
    }
}

@Composable
private fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

private fun previewTask(id: String, title: String, status: TaskStatus, due: DueDate?) = Task(
    id = id,
    userId = "u1",
    title = title,
    description = null,
    notes = null,
    status = status,
    priority = TaskPriority.HIGH,
    due = due,
    estimatedDurationMinutes = 30,
    categoryId = null,
    completedAt = if (status == TaskStatus.COMPLETED) Instant.EPOCH else null,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH
)

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun TasksPreview() {
    LifeOSTheme {
        Surface {
            TasksContent(
                state = TasksUiState.Content(
                    listOf(
                        TaskRow(
                            previewTask(
                                "1",
                                "Write the report",
                                TaskStatus.TODO,
                                DueDate.At(Instant.EPOCH)
                            ),
                            isOverdue = true
                        ),
                        TaskRow(
                            previewTask("2", "Book the dentist", TaskStatus.COMPLETED, null),
                            false
                        )
                    )
                ),
                onEvent = {},
                onAddTask = {},
                onOpenTask = {}
            )
        }
    }
}
