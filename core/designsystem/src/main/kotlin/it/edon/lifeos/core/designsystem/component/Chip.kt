package it.edon.lifeos.core.designsystem.component

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/** One choice of a small group where exactly one is selected, such as a priority. */
@Composable
fun LifeOSChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = LifeOSTheme.typography.labelLarge) },
        modifier = modifier,
        enabled = enabled,
        shape = LifeOSTheme.shapes.large
    )
}
