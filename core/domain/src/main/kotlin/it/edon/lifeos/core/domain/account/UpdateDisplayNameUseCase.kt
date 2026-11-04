package it.edon.lifeos.core.domain.account

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.auth.AuthFailure
import it.edon.lifeos.core.domain.auth.AuthValidation
import it.edon.lifeos.core.domain.repository.AuthRepository
import javax.inject.Inject

/** Changes the name shown for the account. A blank name removes it. */
class UpdateDisplayNameUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(displayName: String): Outcome<Unit, AuthFailure> {
        val issue = AuthValidation.displayNameIssue(displayName)
        if (issue != null) {
            return Outcome.Failure(AuthFailure.InvalidInput(displayName = issue))
        }
        val name = displayName.trim().ifEmpty { null }
        return when (val outcome = repository.updateDisplayName(name)) {
            is Outcome.Success -> outcome
            is Outcome.Failure -> Outcome.Failure(AuthFailure.Rejected(outcome.error))
        }
    }
}
