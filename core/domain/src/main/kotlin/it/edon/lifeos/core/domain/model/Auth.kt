package it.edon.lifeos.core.domain.model

/** The signed-in person. [id] is the owner identifier stored on every user-owned record. */
data class AuthUser(val id: String, val email: String?, val displayName: String?)

sealed interface AuthState {
    /** The stored session has not been read yet. */
    data object Loading : AuthState

    data object SignedOut : AuthState

    data class SignedIn(val user: AuthUser) : AuthState
}

/**
 * Outcome of a successful sign-up. When no confirmation is needed the user is signed in and
 * [AuthState] reflects it; there is no user to return here.
 */
data class SignUpResult(
    /** True when the account exists but cannot be used until the email address is confirmed. */
    val requiresEmailConfirmation: Boolean
)

/** Expected ways in which an authentication operation can fail. */
sealed interface AuthError {
    data object InvalidCredentials : AuthError

    data object EmailAlreadyRegistered : AuthError

    data object WeakPassword : AuthError

    data object EmailNotConfirmed : AuthError

    /** The server rejected the format of what was sent, for example the email address. */
    data object InvalidInput : AuthError

    data object RateLimited : AuthError

    /** The server could not be reached. */
    data object Network : AuthError

    data object Unknown : AuthError
}
