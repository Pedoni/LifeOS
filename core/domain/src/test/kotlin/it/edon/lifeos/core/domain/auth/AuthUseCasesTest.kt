package it.edon.lifeos.core.domain.auth

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.model.SignUpResult
import it.edon.lifeos.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class RecordingAuthRepository : AuthRepository {
    var calls = 0
    var lastEmail: String? = null
    var lastPassword: String? = null
    var lastDisplayName: String? = "not called"
    var signInResult: Outcome<AuthUser, AuthError> = Outcome.Success(AuthUser("u1", "a@b.c", null))
    var signUpResult: Outcome<SignUpResult, AuthError> = Outcome.Success(SignUpResult(false))

    override val authState: Flow<AuthState> = emptyFlow()

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String?
    ): Outcome<SignUpResult, AuthError> {
        calls++
        lastEmail = email
        lastPassword = password
        lastDisplayName = displayName
        return signUpResult
    }

    override suspend fun signIn(email: String, password: String): Outcome<AuthUser, AuthError> {
        calls++
        lastEmail = email
        lastPassword = password
        return signInResult
    }

    override suspend fun signOut(): Outcome<Unit, AuthError> = Outcome.Success(Unit)

    override suspend fun requestPasswordReset(email: String): Outcome<Unit, AuthError> =
        Outcome.Success(Unit)

    override suspend fun updateDisplayName(displayName: String?): Outcome<Unit, AuthError> =
        Outcome.Success(Unit)

    override suspend fun deleteAccount(): Outcome<Unit, AuthError> = Outcome.Success(Unit)
}

class AuthUseCasesTest {
    private val repository = RecordingAuthRepository()
    private val signIn = SignInUseCase(repository)
    private val signUp = SignUpUseCase(repository)

    @Test
    fun signInWithInvalidInputNeverReachesTheRepository() = runTest {
        val outcome = signIn("not-an-email", "")

        assertEquals(
            Outcome.Failure(AuthFailure.InvalidInput(EmailIssue.INVALID, PasswordIssue.BLANK)),
            outcome
        )
        assertEquals(0, repository.calls)
    }

    @Test
    fun signInDoesNotApplyTheNewPasswordRulesToAnExistingPassword() = runTest {
        signIn("a@b.c", "short")

        assertEquals(1, repository.calls)
        assertEquals("short", repository.lastPassword)
    }

    @Test
    fun signInTrimsTheEmailButNotThePassword() = runTest {
        signIn("  a@b.c  ", " secret ")

        assertEquals("a@b.c", repository.lastEmail)
        assertEquals(" secret ", repository.lastPassword)
    }

    @Test
    fun signInWrapsARepositoryFailure() = runTest {
        repository.signInResult = Outcome.Failure(AuthError.InvalidCredentials)

        assertEquals(
            Outcome.Failure(AuthFailure.Rejected(AuthError.InvalidCredentials)),
            signIn("a@b.c", "secret")
        )
    }

    @Test
    fun signInReturnsTheUser() = runTest {
        assertEquals(Outcome.Success(AuthUser("u1", "a@b.c", null)), signIn("a@b.c", "secret"))
    }

    @Test
    fun signUpReportsEveryInvalidFieldAtOnce() = runTest {
        val outcome = signUp("nope", "short", "a".repeat(101))

        assertEquals(
            Outcome.Failure(
                AuthFailure.InvalidInput(
                    EmailIssue.INVALID,
                    PasswordIssue.TOO_SHORT,
                    DisplayNameIssue.TOO_LONG
                )
            ),
            outcome
        )
        assertEquals(0, repository.calls)
    }

    @Test
    fun signUpAppliesTheNewPasswordRules() = runTest {
        assertEquals(
            Outcome.Failure(AuthFailure.InvalidInput(password = PasswordIssue.TOO_SHORT)),
            signUp("a@b.c", "1234567", "")
        )
    }

    @Test
    fun aBlankDisplayNameIsSentAsAbsent() = runTest {
        signUp("a@b.c", "12345678", "   ")

        assertEquals(null, repository.lastDisplayName)
    }

    @Test
    fun aDisplayNameIsTrimmed() = runTest {
        signUp("  a@b.c ", "12345678", "  Ada  ")

        assertEquals("Ada", repository.lastDisplayName)
        assertEquals("a@b.c", repository.lastEmail)
    }

    @Test
    fun signUpWrapsARepositoryFailure() = runTest {
        repository.signUpResult = Outcome.Failure(AuthError.EmailAlreadyRegistered)

        assertEquals(
            Outcome.Failure(AuthFailure.Rejected(AuthError.EmailAlreadyRegistered)),
            signUp("a@b.c", "12345678", "")
        )
    }

    @Test
    fun signUpReportsWhetherTheEmailMustBeConfirmed() = runTest {
        repository.signUpResult = Outcome.Success(SignUpResult(requiresEmailConfirmation = true))

        assertEquals(
            Outcome.Success(SignUpResult(true)),
            signUp("a@b.c", "12345678", "")
        )
    }
}
