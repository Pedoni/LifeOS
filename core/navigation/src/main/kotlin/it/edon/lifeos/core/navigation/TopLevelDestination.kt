package it.edon.lifeos.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector

/** Destinations shown in the bottom navigation bar, in display order. */
enum class TopLevelDestination(
    val route: Any,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val deepLinkPath: String
) {
    Home(
        route = HomeRoute,
        labelRes = R.string.nav_home,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        deepLinkPath = "home"
    ),
    Tasks(
        route = TasksRoute,
        labelRes = R.string.nav_tasks,
        icon = Icons.Outlined.TaskAlt,
        selectedIcon = Icons.Filled.TaskAlt,
        deepLinkPath = "tasks"
    ),
    Calendar(
        route = CalendarRoute,
        labelRes = R.string.nav_calendar,
        icon = Icons.Outlined.CalendarMonth,
        selectedIcon = Icons.Filled.CalendarMonth,
        deepLinkPath = "calendar"
    ),
    Habits(
        route = HabitsRoute,
        labelRes = R.string.nav_habits,
        icon = Icons.Outlined.Repeat,
        selectedIcon = Icons.Filled.Repeat,
        deepLinkPath = "habits"
    ),
    More(
        route = MoreRoute,
        labelRes = R.string.nav_more,
        icon = Icons.Outlined.Apps,
        selectedIcon = Icons.Filled.Apps,
        deepLinkPath = "more"
    )
}
