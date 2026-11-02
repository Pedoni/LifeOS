package it.edon.lifeos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import it.edon.lifeos.core.navigation.CalendarRoute
import it.edon.lifeos.core.navigation.HabitsRoute
import it.edon.lifeos.core.navigation.HomeRoute
import it.edon.lifeos.core.navigation.MoreRoute
import it.edon.lifeos.core.navigation.TasksRoute
import it.edon.lifeos.core.navigation.TopLevelDestination
import it.edon.lifeos.core.navigation.deepLink

/**
 * Navigation graph of the app. Every destination is still a placeholder: feature modules will
 * contribute their own graphs here as they are built.
 */
@Composable
fun LifeOSNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        placeholderDestination<HomeRoute>(TopLevelDestination.Home)
        placeholderDestination<TasksRoute>(TopLevelDestination.Tasks)
        placeholderDestination<CalendarRoute>(TopLevelDestination.Calendar)
        placeholderDestination<HabitsRoute>(TopLevelDestination.Habits)
        placeholderDestination<MoreRoute>(TopLevelDestination.More)
    }
}

private inline fun <reified T : Any> NavGraphBuilder.placeholderDestination(
    destination: TopLevelDestination
) {
    composable<T>(
        deepLinks = listOf(navDeepLink<T>(basePath = destination.deepLink))
    ) {
        PlaceholderScreen(destination = destination)
    }
}
