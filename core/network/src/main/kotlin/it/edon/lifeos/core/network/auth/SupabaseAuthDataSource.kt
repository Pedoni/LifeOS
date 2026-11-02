package it.edon.lifeos.core.network.auth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.RestException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private const val DISPLAY_NAME_KEY = "full_name"

internal class SupabaseAuthDataSource(private val auth: Auth) : AuthRemoteDataSource {

    override val sessionState: Flow<RemoteSessionState> = auth.sessionStatus.map { status ->
        when (status) {
            SessionStatus.Initializing -> RemoteSessionState.Initializing

            is SessionStatus.NotAuthenticated -> RemoteSessionState.SignedOut

            is SessionStatus.Authenticated -> status.session.user.toSessionState()

            // The library reports this only when renewing the session failed because the network
            // or the server was unavailable. The stored session is still valid, so the user stays
            // signed in and the app keeps working offline.
            is SessionStatus.RefreshFailure ->
                auth.sessionManager.loadSessionOrNull()?.user.toSessionState()
        }
    }

    override suspend fun signUp(email: String, password: String): RemoteSignUp = remoteCall {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        // Without a session the account is waiting for its email to be confirmed.
        RemoteSignUp(requiresEmailConfirmation = auth.currentSessionOrNull() == null)
    }

    override suspend fun signIn(email: String, password: String): RemoteUser = remoteCall {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        auth.currentUserOrNull()?.toRemoteUser() ?: throw RemoteAuthException.Unknown(null)
    }

    override suspend fun signOut() = remoteCall {
        auth.signOut()
    }

    override suspend fun requestPasswordReset(email: String) = remoteCall {
        auth.resetPasswordForEmail(email)
    }
}

private fun UserInfo?.toSessionState(): RemoteSessionState =
    this?.let { RemoteSessionState.SignedIn(it.toRemoteUser()) } ?: RemoteSessionState.SignedOut

private fun UserInfo.toRemoteUser() = RemoteUser(
    id = id,
    email = email,
    displayName = userMetadata?.get(DISPLAY_NAME_KEY)?.jsonPrimitive?.contentOrNull
)

/**
 * Runs [block] and translates what can go wrong into a [RemoteAuthException]. Anything not
 * recognized becomes `Unknown`: the user is told that something went wrong and can retry, which
 * is the recovery for an unexpected failure. Cancellation is never swallowed.
 */
@Suppress("TooGenericExceptionCaught")
private suspend inline fun <T> remoteCall(block: () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: RemoteAuthException) {
    throw e
} catch (e: RestException) {
    throw classifyAuthError(code = e.error, statusCode = e.statusCode, cause = e)
} catch (e: IOException) {
    throw RemoteAuthException.Network(e)
} catch (e: Exception) {
    throw RemoteAuthException.Unknown(e::class.simpleName, e)
}
