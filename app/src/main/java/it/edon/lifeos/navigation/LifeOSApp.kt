package it.edon.lifeos.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.edon.lifeos.core.designsystem.component.LifeOSLoadingState
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.feature.auth.AuthNavHost

private enum class RootContent { Loading, Auth, Main }

/** Root of the UI: it shows the sign-in flow or the app depending on who is signed in. */
@Composable
fun LifeOSApp(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val authState by sessionViewModel.authState.collectAsStateWithLifecycle()
    LifeOSRoot(authState = authState, onSignOut = sessionViewModel::signOut)
}

@Composable
internal fun LifeOSRoot(authState: AuthState, onSignOut: () -> Unit) {
    // The content depends on the kind of state, not on who is signed in, so changing user data
    // does not replay the transition.
    val content = when (authState) {
        AuthState.Loading -> RootContent.Loading
        AuthState.SignedOut -> RootContent.Auth
        is AuthState.SignedIn -> RootContent.Main
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = content,
            animationSpec = tween(
                durationMillis = LifeOSTheme.motion.durationMedium,
                easing = LifeOSTheme.motion.easing
            ),
            label = "root"
        ) { target ->
            when (target) {
                RootContent.Loading -> LifeOSLoadingState()
                RootContent.Auth -> AuthNavHost()
                RootContent.Main -> MainContent(onSignOut = onSignOut)
            }
        }
    }
}
