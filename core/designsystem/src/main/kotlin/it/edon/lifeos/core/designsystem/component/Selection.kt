package it.edon.lifeos.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/** One choice of a group where exactly one can be selected. The whole row is the tap target. */
@Composable
fun LifeOSRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = LifeOSTheme.spacing.minTouchTarget)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.md)
    ) {
        // The row handles the click and the semantics, so the button itself is not clickable.
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Text(
            text = label,
            style = LifeOSTheme.typography.bodyLarge,
            color = LifeOSTheme.colors.onSurface
        )
    }
}

/** A setting that is either on or off. The whole row is the tap target. */
@Composable
fun LifeOSSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = LifeOSTheme.spacing.minTouchTarget)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.md)
    ) {
        Column(modifier = Modifier.weight(1f).padding(vertical = LifeOSTheme.spacing.sm)) {
            Text(
                text = label,
                style = LifeOSTheme.typography.bodyLarge,
                color = LifeOSTheme.colors.onSurface
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = LifeOSTheme.typography.bodyMedium,
                    color = LifeOSTheme.colors.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}
