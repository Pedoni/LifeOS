package it.edon.lifeos.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import it.edon.lifeos.core.designsystem.component.LifeOSChoiceChip
import it.edon.lifeos.core.designsystem.component.LifeOSColorSwatch
import it.edon.lifeos.core.designsystem.component.LifeOSFormDialog
import it.edon.lifeos.core.designsystem.component.LifeOSTextButton
import it.edon.lifeos.core.designsystem.component.LifeOSTextField
import it.edon.lifeos.core.designsystem.theme.CategoryColors
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.designsystem.theme.categoryColorOf
import it.edon.lifeos.core.domain.model.Category
import it.edon.lifeos.core.domain.task.CategoryNameIssue

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CategorySection(form: TaskEditorUiState.Form, onEvent: (TaskEditorEvent) -> Unit) {
    Section(title = stringResource(R.string.task_editor_section_category)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.sm)) {
            LifeOSChoiceChip(
                label = stringResource(R.string.task_editor_category_none),
                selected = form.categoryId == null,
                onClick = { onEvent(TaskEditorEvent.CategorySelected(null)) },
                enabled = !form.isSaving
            )
            form.categories.forEach { category ->
                LifeOSChoiceChip(
                    label = category.name,
                    selected = form.categoryId == category.id,
                    onClick = { onEvent(TaskEditorEvent.CategorySelected(category.id)) },
                    enabled = !form.isSaving,
                    dotColor = category.dotColor()
                )
            }
        }
        LifeOSTextButton(
            text = stringResource(R.string.task_editor_category_new),
            onClick = { onEvent(TaskEditorEvent.ShowNewCategory) },
            enabled = !form.isSaving
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NewCategoryDialog(draft: NewCategoryDraft, onEvent: (TaskEditorEvent) -> Unit) {
    LifeOSFormDialog(
        title = stringResource(R.string.task_editor_new_category_title),
        confirmLabel = stringResource(R.string.task_editor_new_category_create),
        dismissLabel = stringResource(R.string.task_editor_cancel),
        onConfirm = { onEvent(TaskEditorEvent.ConfirmNewCategory) },
        onDismiss = { onEvent(TaskEditorEvent.DismissNewCategory) },
        isBusy = draft.isSaving
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.md)) {
            LifeOSTextField(
                value = draft.name,
                onValueChange = { onEvent(TaskEditorEvent.NewCategoryNameChanged(it)) },
                label = stringResource(R.string.task_editor_new_category_name),
                enabled = !draft.isSaving,
                isError = draft.issue != null,
                supportingText = draft.issue?.message(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier.fillMaxWidth()
            )
            FlowRow {
                CategoryColors.forEachIndexed { index, option ->
                    LifeOSColorSwatch(
                        color = option.color,
                        selected = draft.colorHex == option.hex,
                        onClick = { onEvent(TaskEditorEvent.NewCategoryColorSelected(option.hex)) },
                        description = stringResource(
                            R.string.task_editor_new_category_color_description,
                            index + 1,
                            CategoryColors.size
                        ),
                        enabled = !draft.isSaving
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryNameIssue.message(): String = when (this) {
    CategoryNameIssue.BLANK -> stringResource(R.string.task_editor_issue_category_blank)

    CategoryNameIssue.TOO_LONG -> stringResource(
        R.string.task_editor_issue_category_too_long,
        Category.MAX_NAME_LENGTH
    )

    CategoryNameIssue.ALREADY_EXISTS -> stringResource(R.string.task_editor_issue_category_exists)
}

internal fun Category.dotColor() = categoryColorOf(color)
