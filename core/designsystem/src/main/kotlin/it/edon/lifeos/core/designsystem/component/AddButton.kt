package it.edon.lifeos.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Floating button that creates something new. The [label] is always visible, never icon-only.
 * Material hides the label of an extended button from screen readers, so it is repeated as the
 * description.
 */
@Composable
fun LifeOSAddButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = label },
        icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) },
        text = { Text(text = label) }
    )
}
