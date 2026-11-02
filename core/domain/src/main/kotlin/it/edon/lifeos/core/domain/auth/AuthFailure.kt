package it.edon.lifeos.core.domain.auth

import it.edon.lifeos.core.domain.model.AuthError

/** Why signing up or in did not go through. */
sealed interface AuthFailure {
    /** Nothing was sent: at least one field broke a rule of [AuthValidation]. */
    data class InvalidInput(
        val email: EmailIssue? = null,
        val password: PasswordIssue? = null,
        val displayName: DisplayNameIssue? = null
    ) : AuthFailure

    /** The input was valid but the attempt failed. */
    data class Rejected(val error: AuthError) : AuthFailure
}
