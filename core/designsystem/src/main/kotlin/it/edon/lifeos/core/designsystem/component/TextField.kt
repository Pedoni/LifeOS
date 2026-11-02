package it.edon.lifeos.core.designsystem.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/**
 * Single-purpose text input. When [isError] is true, [supportingText] should explain how to
 * fix the problem. [contentType] tells password managers and autofill what the field holds.
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
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    contentType: ContentType? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = if (contentType == null) {
            modifier
        } else {
            modifier.semantics { this.contentType = contentType }
        },
        enabled = enabled,
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        label = { Text(text = label) },
        trailingIcon = trailingIcon,
        supportingText = supportingText?.let { { Text(text = it) } },
        shape = LifeOSTheme.shapes.medium
    )
}
