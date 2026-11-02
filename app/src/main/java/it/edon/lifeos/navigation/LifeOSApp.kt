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

/** Root of the UI: bottom bar plus the navigation host. */
@Composable
fun LifeOSApp() {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    // The bar belongs to the top-level destinations only, not to their detail screens.
    val showBottomBar = TopLevelDestination.entries.any { currentDestination.isInHierarchyOf(it) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
            modifier = Modifier.padding(innerPadding)
        )
    }
}
