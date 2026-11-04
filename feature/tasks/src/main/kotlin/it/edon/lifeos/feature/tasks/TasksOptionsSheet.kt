package it.edon.lifeos.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import it.edon.lifeos.core.designsystem.component.LifeOSBottomSheet
import it.edon.lifeos.core.designsystem.component.LifeOSChoiceChip
import it.edon.lifeos.core.designsystem.component.LifeOSSwitchRow
import it.edon.lifeos.core.designsystem.component.LifeOSTextButton
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.model.TaskPriority
import it.edon.lifeos.core.domain.task.SortDirection
import it.edon.lifeos.core.domain.task.TaskListQuery
import it.edon.lifeos.core.domain.task.TaskSortField

/** Sort order, grouping and filters of the list; changes apply at once. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TasksOptionsSheet(
    query: TaskListQuery,
    categories: List<Category>,
    onEvent: (TasksEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LifeOSBottomSheet(onDismiss = { onEvent(TasksEvent.DismissOptions) }, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.lg)) {
            OptionGroup(title = stringResource(R.string.tasks_options_sort_by)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.sm)) {
                    TaskSortField.entries.forEach { field ->
                        LifeOSChoiceChip(
                            label = stringResource(field.labelRes()),
                            selected = query.sort.field == field,
                            onClick = { onEvent(TasksEvent.SortFieldSelected(field)) }
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.sm)) {
                    SortDirection.entries.forEach { direction ->
                        LifeOSChoiceChip(
                            label = stringResource(direction.labelRes()),
                            selected = query.sort.direction == direction,
                            onClick = { onEvent(TasksEvent.SortDirectionSelected(direction)) }
                        )
                    }
                }
            }
            LifeOSSwitchRow(
                label = stringResource(R.string.tasks_options_group_by_priority),
                checked = query.groupByPriority,
                onCheckedChange = { onEvent(TasksEvent.GroupByPriorityChanged(it)) }
            )
            OptionGroup(title = stringResource(R.string.tasks_options_filter_priority)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.sm)) {
                    TaskPriority.entries.forEach { priority ->
                        LifeOSChoiceChip(
                            label = stringResource(priority.labelRes()),
                            selected = priority in query.filter.priorities,
                            onClick = { onEvent(TasksEvent.PriorityToggled(priority)) }
                        )
                    }
                }
            }
            if (categories.isNotEmpty()) {
                OptionGroup(title = stringResource(R.string.tasks_options_filter_category)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.sm)) {
                        LifeOSChoiceChip(
                            label = stringResource(R.string.tasks_options_any_category),
                            selected = query.filter.categoryId == null,
                            onClick = { onEvent(TasksEvent.CategoryFilterSelected(null)) }
                        )
                        categories.forEach { category ->
                            LifeOSChoiceChip(
                                label = category.name,
                                selected = query.filter.categoryId == category.id,
                                onClick = {
                                    onEvent(TasksEvent.CategoryFilterSelected(category.id))
                                },
                                dotColor = category.dotColor()
                            )
                        }
                    }
                }
            }
            LifeOSTextButton(
                text = stringResource(R.string.tasks_reset),
                onClick = { onEvent(TasksEvent.ResetQuery) },
                enabled = query.isCustomized
            )
        }
    }
}

@Composable
private fun OptionGroup(title: String, content: @Composable () -> Unit) {
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
