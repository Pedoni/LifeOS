package it.edon.lifeos.core.data.repository

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.model.SignUpResult
import it.edon.lifeos.core.network.auth.AuthRemoteDataSource
import it.edon.lifeos.core.network.auth.RemoteAuthException
import it.edon.lifeos.core.network.auth.RemoteSessionState
import it.edon.lifeos.core.network.auth.RemoteSignUp
import it.edon.lifeos.core.network.auth.RemoteUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

private class FakeAuthRemoteDataSource : AuthRemoteDataSource {
    val state = MutableStateFlow<RemoteSessionState>(RemoteSessionState.Initializing)
    var failure: Throwable? = null
    var requiresConfirmation = false
    var lastDisplayName: String? = null

    override val sessionState: Flow<RemoteSessionState> = state

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String?
    ): RemoteSignUp {
        lastDisplayName = displayName
        failure?.let { throw it }
        return RemoteSignUp(requiresConfirmation)
    }

    override suspend fun signIn(email: String, password: String): RemoteUser {
        failure?.let { throw it }
        return RemoteUser(id = "u1", email = email, displayName = "Ada")
    }

    override suspend fun signOut() {
        failure?.let { throw it }
    }

    override suspend fun requestPasswordReset(email: String) {
        failure?.let { throw it }
    }
}

class AuthRepositoryImplTest {
    private val remote = FakeAuthRemoteDataSource()
    private val repository = AuthRepositoryImpl(remote)

    @Test
    fun signInReturnsTheMappedUser() = runTest {
        val outcome = repository.signIn("ada@example.com", "secret")

        assertEquals(
            Outcome.Success(AuthUser(id = "u1", email = "ada@example.com", displayName = "Ada")),
            outcome
        )
    }

    @Test
    fun everyRemoteFailureIsMappedToItsDomainError() = runTest {
        val expected = mapOf<RemoteAuthException, AuthError>(
            RemoteAuthException.InvalidCredentials() to AuthError.InvalidCredentials,
            RemoteAuthException.EmailAlreadyRegistered() to AuthError.EmailAlreadyRegistered,
            RemoteAuthException.WeakPassword() to AuthError.WeakPassword,
            RemoteAuthException.EmailNotConfirmed() to AuthError.EmailNotConfirmed,
            RemoteAuthException.InvalidInput() to AuthError.InvalidInput,
            RemoteAuthException.RateLimited() to AuthError.RateLimited,
            RemoteAuthException.Network() to AuthError.Network,
            RemoteAuthException.Unknown("x") to AuthError.Unknown
        )

        expected.forEach { (failure, error) ->
            remote.failure = failure
            assertEquals(Outcome.Failure(error), repository.signIn("a@b.c", "pw"))
        }
    }

    @Test
    fun signUpReportsWhetherTheEmailMustBeConfirmed() = runTest {
        remote.requiresConfirmation = true

        assertEquals(Outcome.Success(SignUpResult(true)), repository.signUp("a@b.c", "pw", null))
    }

    @Test
    fun signUpForwardsTheDisplayName() = runTest {
        repository.signUp("a@b.c", "pw", "Ada")

        assertEquals("Ada", remote.lastDisplayName)
    }

    @Test
    fun signUpFailureIsReportedAsAnError() = runTest {
        remote.failure = RemoteAuthException.EmailAlreadyRegistered()

        assertEquals(
            Outcome.Failure(AuthError.EmailAlreadyRegistered),
            repository.signUp("a@b.c", "pw", null)
        )
    }

    @Test
    fun signOutAndPasswordResetReportNetworkFailures() = runTest {
        remote.failure = RemoteAuthException.Network()

        assertEquals(Outcome.Failure(AuthError.Network), repository.signOut())
        assertEquals(Outcome.Failure(AuthError.Network), repository.requestPasswordReset("a@b.c"))
    }

    @Test
    fun anUnexpectedExceptionIsNotHidden() = runTest {
        remote.failure = IllegalStateException("bug")

        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { repository.signIn("a@b.c", "pw") }
        }
    }

    @Test
    fun theSessionStateIsMapped() = runTest {
        assertEquals(AuthState.Loading, repository.authState.first())

        remote.state.value = RemoteSessionState.SignedOut
        assertEquals(AuthState.SignedOut, repository.authState.first())

        remote.state.value = RemoteSessionState.SignedIn(RemoteUser("u1", "a@b.c", null))
        assertEquals(
            AuthState.SignedIn(AuthUser("u1", "a@b.c", null)),
            repository.authState.first()
        )
    }
}
