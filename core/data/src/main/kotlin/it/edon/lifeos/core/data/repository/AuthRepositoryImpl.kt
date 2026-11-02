package it.edon.lifeos.core.data.repository

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.data.mapper.toDomain
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.model.SignUpResult
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.network.auth.AuthRemoteDataSource
import it.edon.lifeos.core.network.auth.RemoteAuthException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class AuthRepositoryImpl @Inject constructor(private val remote: AuthRemoteDataSource) :
    AuthRepository {

    override val authState: Flow<AuthState> = remote.sessionState.map { it.toDomain() }

    override suspend fun signUp(email: String, password: String): Outcome<SignUpResult, AuthError> =
        outcomeOf { SignUpResult(remote.signUp(email, password).requiresEmailConfirmation) }

    override suspend fun signIn(email: String, password: String): Outcome<AuthUser, AuthError> =
        outcomeOf { remote.signIn(email, password).toDomain() }

    override suspend fun signOut(): Outcome<Unit, AuthError> = outcomeOf { remote.signOut() }

    override suspend fun requestPasswordReset(email: String): Outcome<Unit, AuthError> =
        outcomeOf { remote.requestPasswordReset(email) }
}

// Only the failures the data source promises to report are turned into values. Anything else is
// a bug and is left to propagate instead of being hidden.
private inline fun <T> outcomeOf(block: () -> T): Outcome<T, AuthError> = try {
    Outcome.Success(block())
} catch (e: RemoteAuthException) {
    Outcome.Failure(e.toDomain())
}
