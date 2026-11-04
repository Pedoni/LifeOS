package it.edon.lifeos.feature.tasks

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.model.CategoryType
import it.edon.lifeos.core.domain.model.DueDate
import it.edon.lifeos.core.domain.model.Task
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.model.TaskStatus
import it.edon.lifeos.core.domain.task.TaskListQuery
import java.time.Instant

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
    val work =
        Category("c1", "u1", "Work", "#3F51B5", CategoryType.TASK, Instant.EPOCH, Instant.EPOCH)
    LifeOSTheme {
        Surface {
            TasksContent(
                state = TasksUiState.Content(
                    query = TaskListQuery(),
                    groups = listOf(
                        TaskRowGroup(
                            priority = null,
                            rows = listOf(
                                TaskRow(
                                    previewTask(
                                        "1",
                                        "Write the report",
                                        TaskStatus.TODO,
                                        DueDate.At(Instant.EPOCH)
                                    ),
                                    isOverdue = true,
                                    category = work
                                ),
                                TaskRow(
                                    previewTask(
                                        "2",
                                        "Book the dentist",
                                        TaskStatus.COMPLETED,
                                        null
                                    ),
                                    false
                                )
                            )
                        )
                    ),
                    categories = listOf(work)
                ),
                onEvent = {},
                onAddTask = {},
                onOpenTask = {}
            )
        }
    }
}
