package it.edon.lifeos.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.edon.lifeos.core.designsystem.component.LifeOSNavigationBar
import it.edon.lifeos.core.designsystem.component.LifeOSNavigationBarItem
import it.edon.lifeos.core.navigation.TopLevelDestination
import it.edon.lifeos.core.navigation.isInHierarchyOf
import it.edon.lifeos.core.navigation.navigateToTopLevel

/**
 * What a signed-in person sees: the bottom bar plus the navigation host. It knows nothing about
 * authentication except the way out, so it can be shown and tested on its own.
 */
@Composable
fun MainContent(onSignOut: () -> Unit, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    // The bar belongs to the top-level destinations only, not to their detail screens.
    val showBottomBar = TopLevelDestination.entries.any { currentDestination.isInHierarchyOf(it) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                LifeOSNavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        LifeOSNavigationBarItem(
                            selected = currentDestination.isInHierarchyOf(destination),
                            onClick = { navController.navigateToTopLevel(destination) },
                            icon = destination.icon,
                            selectedIcon = destination.selectedIcon,
                            label = stringResource(destination.labelRes)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LifeOSNavHost(
            navController = navController,
            onSignOut = onSignOut,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
