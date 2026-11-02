package it.edon.lifeos.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.edon.lifeos.feature.auth.signin.SignInScreen
import it.edon.lifeos.feature.auth.signup.SignUpScreen
import kotlinx.serialization.Serializable

@Serializable
internal data object SignInDestination

@Serializable
internal data object SignUpDestination

/**
 * Everything a signed-out person can reach. It has no way out of its own: when signing in or
 * up succeeds the authentication state changes and the app swaps this host for the main one.
 */
@Composable
fun AuthNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SignInDestination,
        modifier = modifier
    ) {
        composable<SignInDestination> {
            SignInScreen(onCreateAccount = { navController.navigate(SignUpDestination) })
        }
        composable<SignUpDestination> {
            SignUpScreen(onBackToSignIn = { navController.popBackStack() })
        }
    }
}
