package it.edon.lifeos.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/** Small round marker of a category color. Decorative: the category name always sits next to it. */
@Composable
fun LifeOSColorDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(LifeOSTheme.spacing.md)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * One color of a group where exactly one is selected. [description] names it for screen readers;
 * the selected one also shows a check mark, so it is not told apart by color alone.
 */
@Composable
fun LifeOSColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(LifeOSTheme.spacing.minTouchTarget)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(LifeOSTheme.spacing.xxl)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (selected) {
                        Modifier.border(
                            width = LifeOSTheme.spacing.xxs,
                            color = LifeOSTheme.colors.onSurface,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}
