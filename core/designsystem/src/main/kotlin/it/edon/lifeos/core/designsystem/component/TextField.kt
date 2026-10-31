package it.edon.lifeos.core.designsystem.component

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/**
 * Single-purpose text input. When [isError] is true, [supportingText] should explain how to
 * fix the problem.
 */
@Composable
fun LifeOSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        singleLine = singleLine,
        label = { Text(text = label) },
        supportingText = supportingText?.let { { Text(text = it) } },
        shape = LifeOSTheme.shapes.medium
    )
}
