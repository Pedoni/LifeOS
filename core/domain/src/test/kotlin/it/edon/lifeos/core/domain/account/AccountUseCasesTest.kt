package it.edon.lifeos.core.domain.account

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.auth.AuthFailure
import it.edon.lifeos.core.domain.auth.AuthValidation
import it.edon.lifeos.core.domain.auth.DisplayNameIssue
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Records the order of the calls that matter, in one shared list. */
private class AccountFakes(private val events: MutableList<String>) {
    var updateResult: Outcome<Unit, AuthError> = Outcome.Success(Unit)
    var deleteResult: Outcome<Unit, AuthError> = Outcome.Success(Unit)
    var signOutResult: Outcome<Unit, AuthError> = Outcome.Success(Unit)
    var lastDisplayName: String? = "not called"
    var updateCalls = 0

    val auth = object : AuthRepository {
        override val authState: Flow<AuthState> = emptyFlow()

        override suspend fun signUp(email: String, password: String, displayName: String?) =
            error("not used")

        override suspend fun signIn(email: String, password: String) = error("not used")

        override suspend fun signOut(): Outcome<Unit, AuthError> {
            events += "signOut"
            return signOutResult
        }

        override suspend fun requestPasswordReset(email: String) = error("not used")

        override suspend fun updateDisplayName(displayName: String?): Outcome<Unit, AuthError> {
            updateCalls++
            lastDisplayName = displayName
            return updateResult
        }

        override suspend fun deleteAccount(): Outcome<Unit, AuthError> {
            events += "deleteAccount"
            return deleteResult
        }
    }

    val userData = object : UserDataRepository {
        override suspend fun deleteAllFor(userId: String) {
            events += "deleteLocal:$userId"
        }
    }
}

class AccountUseCasesTest {
    private val events = mutableListOf<String>()
    private val fakes = AccountFakes(events)
    private val updateDisplayName = UpdateDisplayNameUseCase(fakes.auth)
    private val deleteAccount = DeleteAccountUseCase(fakes.auth, fakes.userData)

    @Test
    fun aTooLongNameNeverReachesTheRepository() = runTest {
        val outcome = updateDisplayName("a".repeat(AuthValidation.MAX_DISPLAY_NAME_LENGTH + 1))

        assertEquals(
            Outcome.Failure(AuthFailure.InvalidInput(displayName = DisplayNameIssue.TOO_LONG)),
            outcome
        )
        assertEquals(0, fakes.updateCalls)
    }

    @Test
    fun theNameIsTrimmed() = runTest {
        updateDisplayName("  Ada  ")

        assertEquals("Ada", fakes.lastDisplayName)
    }

    @Test
    fun aBlankNameRemovesTheName() = runTest {
        val outcome = updateDisplayName("   ")

        assertEquals(Outcome.Success(Unit), outcome)
        assertEquals(null, fakes.lastDisplayName)
    }

    @Test
    fun aRejectedUpdateIsReported() = runTest {
        fakes.updateResult = Outcome.Failure(AuthError.Network)

        assertEquals(
            Outcome.Failure(AuthFailure.Rejected(AuthError.Network)),
            updateDisplayName("Ada")
        )
    }

    @Test
    fun deletingTheAccountCleansUpInOrder() = runTest {
        val outcome = deleteAccount("u1")

        assertEquals(Outcome.Success(Unit), outcome)
        assertEquals(listOf("deleteAccount", "deleteLocal:u1", "signOut"), events)
    }

    @Test
    fun localDataIsKeptWhenTheServerDoesNotDelete() = runTest {
        fakes.deleteResult = Outcome.Failure(AuthError.Network)

        val outcome = deleteAccount("u1")

        assertEquals(Outcome.Failure(AuthError.Network), outcome)
        assertEquals(listOf("deleteAccount"), events)
    }
}
