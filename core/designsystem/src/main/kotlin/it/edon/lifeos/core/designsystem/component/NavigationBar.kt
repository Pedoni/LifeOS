package it.edon.lifeos.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/** Bottom navigation bar. Fill it with [LifeOSNavigationBarItem]s. */
@Composable
fun LifeOSNavigationBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    NavigationBar(
        modifier = modifier,
        containerColor = LifeOSTheme.colors.surfaceContainer,
        tonalElevation = LifeOSTheme.elevation.none,
        content = content
    )
}

/**
 * One tab of [LifeOSNavigationBar]. The label is always visible, so the icon needs no separate
 * content description: screen readers announce the label and the selected state.
 */
@Composable
fun RowScope.LifeOSNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(imageVector = if (selected) selectedIcon else icon, contentDescription = null)
        },
        modifier = modifier,
        label = { Text(text = label) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = LifeOSTheme.colors.onPrimaryContainer,
            selectedTextColor = LifeOSTheme.colors.onSurface,
            indicatorColor = LifeOSTheme.colors.primaryContainer,
            unselectedIconColor = LifeOSTheme.colors.onSurfaceVariant,
            unselectedTextColor = LifeOSTheme.colors.onSurfaceVariant
        )
    )
}
