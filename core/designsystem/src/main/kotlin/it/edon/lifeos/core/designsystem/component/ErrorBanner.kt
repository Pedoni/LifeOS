package it.edon.lifeos.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/**
 * Inline message for a failed action. It is announced by screen readers when it appears, so the
 * person does not have to find it.
 */
@Composable
fun LifeOSErrorBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = LifeOSTheme.shapes.medium,
        color = LifeOSTheme.colors.errorContainer,
        contentColor = LifeOSTheme.colors.onErrorContainer
    ) {
        Text(
            text = message,
            style = LifeOSTheme.typography.bodyMedium,
            modifier = Modifier.padding(LifeOSTheme.spacing.md)
        )
    }
}
