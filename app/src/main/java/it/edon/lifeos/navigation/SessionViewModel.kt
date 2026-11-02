package it.edon.lifeos.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.edon.lifeos.core.domain.model.AuthState
import it.edon.lifeos.core.domain.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** Who is signed in, as the root of the app needs to know it. */
@HiltViewModel
class SessionViewModel @Inject constructor(private val authRepository: AuthRepository) :
    ViewModel() {
    val authState: StateFlow<AuthState> = authRepository.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AuthState.Loading
    )

    // The outcome is not shown: signing out only ever succeeds from the person's point of view,
    // because the local session is what matters. This goes away with the profile screen.
    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
