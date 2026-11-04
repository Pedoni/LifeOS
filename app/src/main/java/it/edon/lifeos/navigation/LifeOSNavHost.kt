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
import it.edon.lifeos.feature.profile.ProfileRoute
import it.edon.lifeos.feature.profile.profileDestination
import it.edon.lifeos.feature.tasks.TaskEditorRoute
import it.edon.lifeos.feature.tasks.TasksScreen
import it.edon.lifeos.feature.tasks.taskEditorDestination

/**
 * Navigation graph of the signed-in app. Destinations without a feature yet are placeholders:
 * feature modules contribute their own destinations here as they are built.
 */
@Composable
fun LifeOSNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        placeholderDestination<HomeRoute>(TopLevelDestination.Home)
        composable<TasksRoute>(
            deepLinks = listOf(
                navDeepLink<TasksRoute>(basePath = TopLevelDestination.Tasks.deepLink)
            )
        ) {
            TasksScreen(
                onAddTask = { navController.navigate(TaskEditorRoute()) },
                onOpenTask = { navController.navigate(TaskEditorRoute(taskId = it)) }
            )
        }
        placeholderDestination<CalendarRoute>(TopLevelDestination.Calendar)
        placeholderDestination<HabitsRoute>(TopLevelDestination.Habits)
        composable<MoreRoute>(
            deepLinks = listOf(navDeepLink<MoreRoute>(basePath = TopLevelDestination.More.deepLink))
        ) {
            MoreScreen(onOpenProfile = { navController.navigate(ProfileRoute) })
        }
        profileDestination(onBack = { navController.popBackStack() })
        taskEditorDestination(onBack = { navController.popBackStack() })
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
