package it.edon.lifeos.core.network.auth

import kotlinx.coroutines.flow.Flow

data class RemoteUser(val id: String, val email: String?, val displayName: String?)

data class RemoteSignUp(val requiresEmailConfirmation: Boolean)

sealed interface RemoteSessionState {
    data object Initializing : RemoteSessionState

    data object SignedOut : RemoteSessionState

    data class SignedIn(val user: RemoteUser) : RemoteSessionState
}

/**
 * Failures of the authentication backend, already free of any library type. The original
 * exception, when there was one, is kept as the [cause] to help diagnosing; it is never shown
 * to the user.
 */
sealed class RemoteAuthException(message: String, cause: Throwable?) : Exception(message, cause) {
    class InvalidCredentials(cause: Throwable? = null) :
        RemoteAuthException("Invalid credentials", cause)

    class EmailAlreadyRegistered(cause: Throwable? = null) :
        RemoteAuthException("Email already registered", cause)

    class WeakPassword(cause: Throwable? = null) : RemoteAuthException("Weak password", cause)

    class EmailNotConfirmed(cause: Throwable? = null) :
        RemoteAuthException("Email not confirmed", cause)

    class InvalidInput(cause: Throwable? = null) : RemoteAuthException("Invalid input", cause)

    class RateLimited(cause: Throwable? = null) : RemoteAuthException("Rate limited", cause)

    class Network(cause: Throwable? = null) : RemoteAuthException("Network failure", cause)

    /** [code] is the backend error code, when there was one. */
    class Unknown(val code: String?, cause: Throwable? = null) :
        RemoteAuthException("Unknown authentication failure: $code", cause)
}

/** Talks to the authentication backend. Every failure is reported as a [RemoteAuthException]. */
interface AuthRemoteDataSource {
    val sessionState: Flow<RemoteSessionState>

    suspend fun signUp(email: String, password: String): RemoteSignUp

    suspend fun signIn(email: String, password: String): RemoteUser

    suspend fun signOut()

    suspend fun requestPasswordReset(email: String)
}
