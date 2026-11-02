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

    suspend fun signUp(email: String, password: String): Outcome<SignUpResult, AuthError>

    suspend fun signIn(email: String, password: String): Outcome<AuthUser, AuthError>

    suspend fun signOut(): Outcome<Unit, AuthError>

    suspend fun requestPasswordReset(email: String): Outcome<Unit, AuthError>
}
