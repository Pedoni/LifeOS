package it.edon.lifeos

import it.edon.lifeos.core.domain.model.ThemeMode
import it.edon.lifeos.core.domain.model.UserPreferences

/** What the root of the app needs before drawing: the user's appearance preferences. */
sealed interface MainUiState {
    /** Preferences not read yet: the system appearance is used meanwhile. */
    data object Loading : MainUiState

    data class Success(val preferences: UserPreferences) : MainUiState
}

fun MainUiState.shouldUseDarkTheme(isSystemInDarkTheme: Boolean): Boolean = when (this) {
    MainUiState.Loading -> isSystemInDarkTheme

    is MainUiState.Success -> when (preferences.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}

fun MainUiState.shouldUseDynamicColor(): Boolean =
    (this as? MainUiState.Success)?.preferences?.useDynamicColor ?: false
