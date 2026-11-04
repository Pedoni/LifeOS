package it.edon.lifeos.feature.auth.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.auth.AuthFailure
import it.edon.lifeos.core.domain.auth.EmailIssue
import it.edon.lifeos.core.domain.auth.PasswordIssue
import it.edon.lifeos.core.domain.auth.SignInUseCase
import it.edon.lifeos.core.domain.model.AuthError
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailIssue: EmailIssue? = null,
    val passwordIssue: PasswordIssue? = null,
    val error: AuthError? = null,
    val isSubmitting: Boolean = false
)

sealed interface SignInEvent {
    data class EmailChanged(val value: String) : SignInEvent

    data class PasswordChanged(val value: String) : SignInEvent

    data object TogglePasswordVisibility : SignInEvent

    data object Submit : SignInEvent
}

@HiltViewModel
class SignInViewModel @Inject constructor(private val signIn: SignInUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEvent(event: SignInEvent) {
        when (event) {
            // Changing a field takes away what was said about it, and the general error.
            is SignInEvent.EmailChanged -> _uiState.update {
                it.copy(email = event.value, emailIssue = null, error = null)
            }

            is SignInEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.value, passwordIssue = null, error = null)
            }

            SignInEvent.TogglePasswordVisibility -> _uiState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }

            SignInEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _uiState.value
        if (current.isSubmitting) return
        _uiState.update {
            it.copy(isSubmitting = true, error = null, emailIssue = null, passwordIssue = null)
        }
        viewModelScope.launch {
            val outcome = signIn(current.email, current.password)
            // On success nothing else is needed here: the authentication state changes and the
            // app replaces this screen.
            _uiState.update { state ->
                when (outcome) {
                    is Outcome.Success -> state.copy(isSubmitting = false)
                    is Outcome.Failure -> state.withFailure(outcome.error)
                }
            }
        }
    }
}

private fun SignInUiState.withFailure(failure: AuthFailure): SignInUiState = when (failure) {
    is AuthFailure.InvalidInput -> copy(
        isSubmitting = false,
        emailIssue = failure.email,
        passwordIssue = failure.password
    )

    is AuthFailure.Rejected -> copy(isSubmitting = false, error = failure.error)
}
