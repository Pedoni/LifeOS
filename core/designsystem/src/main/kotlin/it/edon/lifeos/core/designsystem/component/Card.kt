package it.edon.lifeos.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/**
 * Tonal container: no border and no shadow, separated from the background only by its surface
 * color. Pass [onClick] to make the whole card tappable.
 */
@Composable
fun LifeOSCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = LifeOSTheme.colors.surfaceContainerLow)
    val shape = LifeOSTheme.shapes.medium
    val inner: @Composable ColumnScope.() -> Unit = {
        Column(modifier = Modifier.padding(LifeOSTheme.spacing.lg), content = content)
    }
    if (onClick == null) {
        Card(modifier = modifier, shape = shape, colors = colors, content = inner)
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            content = inner
        )
    }
}
