package it.edon.lifeos.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/**
 * Asks for confirmation before an action. Use [isDestructive] for actions that cannot be undone:
 * the confirm button is then drawn as a warning. Both buttons are disabled while [isBusy].
 */
@Composable
fun LifeOSConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    isBusy: Boolean = false
) {
    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        modifier = modifier,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isBusy,
                colors = if (isDestructive) {
                    ButtonDefaults.textButtonColors(contentColor = LifeOSTheme.colors.error)
                } else {
                    ButtonDefaults.textButtonColors()
                }
            ) {
                Text(text = confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) {
                Text(text = dismissLabel)
            }
        }
    )
}
