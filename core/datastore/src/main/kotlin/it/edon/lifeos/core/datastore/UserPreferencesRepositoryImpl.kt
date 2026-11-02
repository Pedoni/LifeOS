package it.edon.lifeos.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import it.edon.lifeos.core.domain.model.ThemeMode
import it.edon.lifeos.core.domain.model.UserPreferences
import it.edon.lifeos.core.domain.repository.UserPreferencesRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val THEME_MODE = stringPreferencesKey("theme_mode")
private val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")

internal class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    // A read failure falls back to the defaults instead of breaking the whole UI: these are
    // cosmetic preferences and the user can set them again. Other errors are not hidden.
    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { it.toUserPreferences() }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    override suspend fun setUseDynamicColor(enabled: Boolean) {
        dataStore.edit { it[USE_DYNAMIC_COLOR] = enabled }
    }
}

private fun Preferences.toUserPreferences(): UserPreferences = UserPreferences(
    themeMode = this[THEME_MODE]
        ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
        ?: ThemeMode.SYSTEM,
    useDynamicColor = this[USE_DYNAMIC_COLOR] ?: false
)
