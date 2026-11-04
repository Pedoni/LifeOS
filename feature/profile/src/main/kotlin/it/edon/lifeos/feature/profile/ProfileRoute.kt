package it.edon.lifeos.feature.profile

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object ProfileRoute

/** Adds the profile screen to a graph. [onBack] is called by the back arrow of the screen. */
fun NavGraphBuilder.profileDestination(onBack: () -> Unit) {
    composable<ProfileRoute> {
        ProfileScreen(onBack = onBack)
    }
}
