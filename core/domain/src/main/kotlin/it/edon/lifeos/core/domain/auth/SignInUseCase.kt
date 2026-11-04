package it.edon.lifeos.core.domain.auth

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.repository.AuthRepository
import javax.inject.Inject

/** Signs in after checking the input, so invalid data never reaches the backend. */
class SignInUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Outcome<AuthUser, AuthFailure> {
        val emailIssue = AuthValidation.emailIssue(email)
        // Only blankness is checked here: the rules for a new password do not apply to one that
        // was set earlier, possibly under older rules.
        val passwordIssue = if (password.isEmpty()) PasswordIssue.BLANK else null
        if (emailIssue != null || passwordIssue != null) {
            return Outcome.Failure(
                AuthFailure.InvalidInput(email = emailIssue, password = passwordIssue)
            )
        }
        return when (val outcome = repository.signIn(email.trim(), password)) {
            is Outcome.Success -> outcome
            is Outcome.Failure -> Outcome.Failure(AuthFailure.Rejected(outcome.error))
        }
    }
}
