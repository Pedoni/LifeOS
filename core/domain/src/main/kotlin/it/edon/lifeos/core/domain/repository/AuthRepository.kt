package it.edon.lifeos.core.domain.repository

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.model.SignUpResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /**
     * The current authentication state. A stored session keeps the user signed in without a
     * connection: failing to renew it because the network is down does not sign anyone out.
     */
    val authState: Flow<AuthState>

    /** [displayName] is optional and is stored with the account. */
    suspend fun signUp(
        email: String,
        password: String,
        displayName: String?
    ): Outcome<SignUpResult, AuthError>

    suspend fun signIn(email: String, password: String): Outcome<AuthUser, AuthError>

    suspend fun signOut(): Outcome<Unit, AuthError>

    suspend fun requestPasswordReset(email: String): Outcome<Unit, AuthError>

    /** A null [displayName] removes it. The new value shows up in [authState]. */
    suspend fun updateDisplayName(displayName: String?): Outcome<Unit, AuthError>

    /**
     * Deletes the account on the server with all its data. The person stays signed in on this
     * device until [signOut] is called, so the caller can clean up local data first.
     */
    suspend fun deleteAccount(): Outcome<Unit, AuthError>
}
