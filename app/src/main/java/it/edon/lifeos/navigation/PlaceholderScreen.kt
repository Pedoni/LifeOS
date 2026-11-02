package it.edon.lifeos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.edon.lifeos.R
import it.edon.lifeos.core.designsystem.component.LifeOSEmptyState
import it.edon.lifeos.core.navigation.TopLevelDestination

/**
 * Stand-in for a destination whose feature has not been built yet. When [onSignOut] is given it
 * also offers to sign out: that is a temporary home for the action until the profile exists.
 */
@Composable
fun PlaceholderScreen(destination: TopLevelDestination, onSignOut: (() -> Unit)? = null) {
    LifeOSEmptyState(
        title = stringResource(destination.labelRes),
        message = stringResource(R.string.placeholder_message),
        actionLabel = onSignOut?.let { stringResource(R.string.placeholder_sign_out) },
        onAction = onSignOut
    )
}
