package it.edon.lifeos.core.domain.account

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.domain.repository.UserDataRepository
import javax.inject.Inject

/**
 * Deletes the account and everything stored for it, then signs out.
 *
 * The order matters: until the server confirms the deletion nothing local is touched, because
 * on this device the data may be the only copy. If the app dies after the server has deleted the
 * account but before the local cleanup, the leftover rows belong to an owner that can no longer
 * sign in and are never shown.
 */
class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDataRepository: UserDataRepository
) {
    suspend operator fun invoke(userId: String): Outcome<Unit, AuthError> {
        val deletion = authRepository.deleteAccount()
        if (deletion is Outcome.Failure) return deletion
        userDataRepository.deleteAllFor(userId)
        return authRepository.signOut()
    }
}
