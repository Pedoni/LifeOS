package it.edon.lifeos.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.edon.lifeos.core.common.Outcome
import it.edon.lifeos.core.domain.account.DeleteAccountUseCase
import it.edon.lifeos.core.domain.account.UpdateDisplayNameUseCase
import it.edon.lifeos.core.domain.auth.AuthFailure
import it.edon.lifeos.core.domain.auth.DisplayNameIssue
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.model.ThemeMode
import it.edon.lifeos.core.domain.model.UserPreferences
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

sealed interface ProfileUiState {
    /** The account is not known yet, or the person has just signed out and the screen is leaving. */
    data object Loading : ProfileUiState

    data class Ready(
        val user: AuthUser,
        val preferences: UserPreferences,
        /** What the name field shows: the unsaved edit, or else the stored name. */
        val displayName: String,
        val canSaveDisplayName: Boolean,
        val displayNameIssue: DisplayNameIssue? = null,
        val error: AuthError? = null,
        val isBusy: Boolean = false,
        val isDeleteDialogVisible: Boolean = false
    ) : ProfileUiState
}

sealed interface ProfileEvent {
    data class DisplayNameChanged(val value: String) : ProfileEvent

    data object SaveDisplayName : ProfileEvent

    data class ThemeModeSelected(val mode: ThemeMode) : ProfileEvent

    data class DynamicColorChanged(val enabled: Boolean) : ProfileEvent

    data object SignOut : ProfileEvent

    data object RequestAccountDeletion : ProfileEvent

    data object DismissAccountDeletion : ProfileEvent

    data object ConfirmAccountDeletion : ProfileEvent
}

/** What only the screen knows: it is not part of the account or of the saved preferences. */
private data class ScreenState(
    val draftDisplayName: String? = null,
    val displayNameIssue: DisplayNameIssue? = null,
    val error: AuthError? = null,
    val isBusy: Boolean = false,
    val isDeleteDialogVisible: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val updateDisplayName: UpdateDisplayNameUseCase,
    private val deleteAccount: DeleteAccountUseCase
) : ViewModel() {
    private val screen = MutableStateFlow(ScreenState())

    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.authState,
        preferencesRepository.preferences,
        screen
    ) { authState, preferences, screen -> toUiState(authState, preferences, screen) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ProfileUiState.Loading
        )

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.DisplayNameChanged -> screen.update {
                it.copy(draftDisplayName = event.value, displayNameIssue = null, error = null)
            }

            ProfileEvent.SaveDisplayName -> saveDisplayName()

            is ProfileEvent.ThemeModeSelected -> viewModelScope.launch {
                preferencesRepository.setThemeMode(event.mode)
            }

            is ProfileEvent.DynamicColorChanged -> viewModelScope.launch {
                preferencesRepository.setUseDynamicColor(event.enabled)
            }

            ProfileEvent.SignOut -> signOut()

            ProfileEvent.RequestAccountDeletion -> screen.update {
                it.copy(isDeleteDialogVisible = true, error = null)
            }

            ProfileEvent.DismissAccountDeletion -> screen.update {
                it.copy(isDeleteDialogVisible = false)
            }

            ProfileEvent.ConfirmAccountDeletion -> deleteCurrentAccount()
        }
    }

    private fun saveDisplayName() {
        val draft = screen.value.draftDisplayName ?: return
        if (screen.value.isBusy) return
        screen.update { it.copy(isBusy = true, error = null, displayNameIssue = null) }
        viewModelScope.launch {
            when (val outcome = updateDisplayName(draft)) {
                // The new name arrives through the account state, so the draft is dropped.
                is Outcome.Success -> screen.update {
                    it.copy(isBusy = false, draftDisplayName = null)
                }

                is Outcome.Failure -> screen.update { it.withFailure(outcome.error) }
            }
        }
    }

    private fun signOut() {
        if (screen.value.isBusy) return
        screen.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            val outcome = authRepository.signOut()
            screen.update {
                it.copy(isBusy = false, error = (outcome as? Outcome.Failure)?.error)
            }
        }
    }

    private fun deleteCurrentAccount() {
        val userId = (uiState.value as? ProfileUiState.Ready)?.user?.id ?: return
        if (screen.value.isBusy) return
        screen.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            val outcome = deleteAccount(userId)
            screen.update {
                it.copy(
                    isBusy = false,
                    isDeleteDialogVisible = false,
                    error = (outcome as? Outcome.Failure)?.error
                )
            }
        }
    }
}

private fun toUiState(
    authState: AuthState,
    preferences: UserPreferences,
    screen: ScreenState
): ProfileUiState {
    val user = (authState as? AuthState.SignedIn)?.user ?: return ProfileUiState.Loading
    val stored = user.displayName.orEmpty()
    val shown = screen.draftDisplayName ?: stored
    return ProfileUiState.Ready(
        user = user,
        preferences = preferences,
        displayName = shown,
        canSaveDisplayName = screen.draftDisplayName != null && shown.trim() != stored,
        displayNameIssue = screen.displayNameIssue,
        error = screen.error,
        isBusy = screen.isBusy,
        isDeleteDialogVisible = screen.isDeleteDialogVisible
    )
}

private fun ScreenState.withFailure(failure: AuthFailure): ScreenState = when (failure) {
    is AuthFailure.InvalidInput -> copy(isBusy = false, displayNameIssue = failure.displayName)
    is AuthFailure.Rejected -> copy(isBusy = false, error = failure.error)
}
