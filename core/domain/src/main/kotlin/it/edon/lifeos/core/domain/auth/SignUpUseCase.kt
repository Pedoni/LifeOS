package it.edon.lifeos.core.domain.auth

import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.model.SignUpResult
import it.edon.lifeos.core.domain.repository.AuthRepository
import javax.inject.Inject

/** Creates an account after checking the input; the display name is optional. */
class SignUpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): Outcome<SignUpResult, AuthFailure> {
        val invalid = AuthFailure.InvalidInput(
            email = AuthValidation.emailIssue(email),
            password = AuthValidation.passwordIssue(password),
            displayName = AuthValidation.displayNameIssue(displayName)
        )
        if (invalid.email != null || invalid.password != null || invalid.displayName != null) {
            return Outcome.Failure(invalid)
        }
        val name = displayName.trim().ifEmpty { null }
        return when (val outcome = repository.signUp(email.trim(), password, name)) {
            is Outcome.Success -> outcome
            is Outcome.Failure -> Outcome.Failure(AuthFailure.Rejected(outcome.error))
        }
    }
}
