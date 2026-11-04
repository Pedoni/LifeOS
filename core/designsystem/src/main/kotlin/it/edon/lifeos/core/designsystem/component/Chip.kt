package it.edon.lifeos.core.designsystem.component

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/**
 * One choice of a small group, such as a priority or a category. A group where only one can be
 * selected is a radio-like use; a group where several can is a filter. [dotColor] adds a color
 * marker before the label; the label always carries the meaning.
 */
@Composable
fun LifeOSChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dotColor: Color? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = LifeOSTheme.typography.labelLarge) },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = dotColor?.let { color -> { LifeOSColorDot(color = color) } },
        shape = LifeOSTheme.shapes.large
    )
}
