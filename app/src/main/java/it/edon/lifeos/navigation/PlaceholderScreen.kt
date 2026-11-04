package it.edon.lifeos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.edon.lifeos.R
import it.edon.lifeos.core.designsystem.component.LifeOSEmptyState
import it.edon.lifeos.core.navigation.TopLevelDestination

/** Stand-in for a destination whose feature has not been built yet. */
@Composable
fun PlaceholderScreen(destination: TopLevelDestination) {
    LifeOSEmptyState(
        title = stringResource(destination.labelRes),
        message = stringResource(R.string.placeholder_message)
    )
}
