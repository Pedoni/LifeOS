package it.edon.lifeos.core.domain.repository

import it.edon.lifeos.core.domain.model.ThemeMode
import it.edon.lifeos.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setUseDynamicColor(enabled: Boolean)
}
