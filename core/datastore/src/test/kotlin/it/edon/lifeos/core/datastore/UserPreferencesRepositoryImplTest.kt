package it.edon.lifeos.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import it.edon.lifeos.core.domain.model.ThemeMode
import it.edon.lifeos.core.domain.model.UserPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * In-memory store. A file-based one is avoided on purpose: DataStore renames a temporary file
 * over the real one, which fails on Windows JVMs and would make these tests depend on the OS.
 */
private class InMemoryPreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences
    ): Preferences = transform(state.value).also { state.value = it }
}

class UserPreferencesRepositoryImplTest {
    private val store = InMemoryPreferencesDataStore()
    private val repository = UserPreferencesRepositoryImpl(store)

    @Test
    fun emitsTheDefaultsWhenNothingWasSaved() = runTest {
        repository.preferences.test {
            assertEquals(UserPreferences(), awaitItem())
        }
    }

    @Test
    fun savedThemeModeIsEmitted() = runTest {
        repository.preferences.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)
            repository.setThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, awaitItem().themeMode)
        }
    }

    @Test
    fun savedDynamicColorIsEmittedAndKeepsTheThemeMode() = runTest {
        repository.setThemeMode(ThemeMode.LIGHT)

        repository.setUseDynamicColor(true)

        repository.preferences.test {
            assertEquals(UserPreferences(ThemeMode.LIGHT, useDynamicColor = true), awaitItem())
        }
    }

    @Test
    fun anUnknownStoredThemeFallsBackToSystem() = runTest {
        store.edit { it[stringPreferencesKey("theme_mode")] = "PURPLE" }

        repository.preferences.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)
        }
    }

    @Test
    fun aReadFailureFallsBackToTheDefaults() = runTest {
        val failing = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flow { throw IOException("disk error") }

            override suspend fun updateData(
                transform: suspend (t: Preferences) -> Preferences
            ): Preferences = throw IOException("disk error")
        }

        UserPreferencesRepositoryImpl(failing).preferences.test {
            assertEquals(UserPreferences(), awaitItem())
            awaitComplete()
        }
    }
}
