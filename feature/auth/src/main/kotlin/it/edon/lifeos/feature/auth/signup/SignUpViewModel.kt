package it.edon.lifeos.feature.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.auth.AuthFailure
import it.edon.lifeos.core.domain.auth.DisplayNameIssue
import it.edon.lifeos.core.domain.auth.EmailIssue
import it.edon.lifeos.core.domain.auth.PasswordIssue
import it.edon.lifeos.core.domain.auth.SignUpUseCase
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.SignUpResult
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignUpUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val displayNameIssue: DisplayNameIssue? = null,
    val emailIssue: EmailIssue? = null,
    val passwordIssue: PasswordIssue? = null,
    val error: AuthError? = null,
    val isSubmitting: Boolean = false,
    /** Set when the account exists but its email must be confirmed before signing in. */
    val confirmationSentTo: String? = null
)

sealed interface SignUpEvent {
    data class DisplayNameChanged(val value: String) : SignUpEvent

    data class EmailChanged(val value: String) : SignUpEvent

    data class PasswordChanged(val value: String) : SignUpEvent

    data object TogglePasswordVisibility : SignUpEvent

    data object Submit : SignUpEvent
}

@HiltViewModel
class SignUpViewModel @Inject constructor(private val signUp: SignUpUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.DisplayNameChanged -> _uiState.update {
                it.copy(displayName = event.value, displayNameIssue = null, error = null)
            }

            is SignUpEvent.EmailChanged -> _uiState.update {
                it.copy(email = event.value, emailIssue = null, error = null)
            }

            is SignUpEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.value, passwordIssue = null, error = null)
            }

            SignUpEvent.TogglePasswordVisibility -> _uiState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }

            SignUpEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _uiState.value
        if (current.isSubmitting) return
        _uiState.update {
            it.copy(
                isSubmitting = true,
                error = null,
                displayNameIssue = null,
                emailIssue = null,
                passwordIssue = null
            )
        }
        viewModelScope.launch {
            val outcome = signUp(current.email, current.password, current.displayName)
            _uiState.update { state ->
                when (outcome) {
                    is Outcome.Success -> state.afterSuccess(outcome.value, current.email.trim())
                    is Outcome.Failure -> state.withFailure(outcome.error)
                }
            }
        }
    }
}

// When no confirmation is needed the person is already signed in and the app replaces this
// screen, so only the "check your email" case needs a state of its own.
private fun SignUpUiState.afterSuccess(result: SignUpResult, email: String): SignUpUiState = copy(
    isSubmitting = false,
    confirmationSentTo = if (result.requiresEmailConfirmation) email else null
)

private fun SignUpUiState.withFailure(failure: AuthFailure): SignUpUiState = when (failure) {
    is AuthFailure.InvalidInput -> copy(
        isSubmitting = false,
        displayNameIssue = failure.displayName,
        emailIssue = failure.email,
        passwordIssue = failure.password
    )

    is AuthFailure.Rejected -> copy(isSubmitting = false, error = failure.error)
}
