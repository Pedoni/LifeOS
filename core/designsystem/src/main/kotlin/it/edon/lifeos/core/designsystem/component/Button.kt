package it.edon.lifeos.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/**
 * Primary action of a screen or form. While [isLoading] the button cannot be pressed and shows
 * a progress indicator in place of the text, which stays available to screen readers.
 */
@Composable
fun LifeOSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = LifeOSTheme.spacing.minTouchTarget)
            .semantics { if (isLoading) contentDescription = text },
        enabled = enabled && !isLoading,
        shape = LifeOSTheme.shapes.large,
        contentPadding = PaddingValues(
            horizontal = LifeOSTheme.spacing.xl,
            vertical = LifeOSTheme.spacing.md
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(LifeOSTheme.spacing.xl),
                strokeWidth = LifeOSTheme.spacing.xxs,
                color = LocalContentColor.current
            )
        } else {
            Text(text = text, style = LifeOSTheme.typography.labelLarge)
        }
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
