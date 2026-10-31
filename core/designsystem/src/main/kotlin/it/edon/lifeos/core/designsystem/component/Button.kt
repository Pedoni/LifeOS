package it.edon.lifeos.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/** Primary action of a screen or form. */
@Composable
fun LifeOSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = LifeOSTheme.spacing.minTouchTarget),
        enabled = enabled,
        shape = LifeOSTheme.shapes.large,
        contentPadding = PaddingValues(
            horizontal = LifeOSTheme.spacing.xl,
            vertical = LifeOSTheme.spacing.md
        )
    ) {
        Text(text = text, style = LifeOSTheme.typography.labelLarge)
    }
}

/** Secondary, low-emphasis action. */
@Composable
fun LifeOSTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = LifeOSTheme.spacing.minTouchTarget),
        enabled = enabled,
        shape = LifeOSTheme.shapes.large
    ) {
        Text(text = text, style = LifeOSTheme.typography.labelLarge)
    }
}
