package it.edon.lifeos

import it.edon.lifeos.core.domain.model.ThemeMode
import it.edon.lifeos.core.domain.model.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainUiStateTest {
    private fun success(mode: ThemeMode, dynamic: Boolean = false) =
        MainUiState.Success(UserPreferences(mode, dynamic))

    @Test
    fun whileLoadingTheSystemAppearanceIsUsed() {
        assertTrue(MainUiState.Loading.shouldUseDarkTheme(isSystemInDarkTheme = true))
        assertFalse(MainUiState.Loading.shouldUseDarkTheme(isSystemInDarkTheme = false))
    }

    @Test
    fun systemModeFollowsTheSystem() {
        assertTrue(success(ThemeMode.SYSTEM).shouldUseDarkTheme(isSystemInDarkTheme = true))
        assertFalse(success(ThemeMode.SYSTEM).shouldUseDarkTheme(isSystemInDarkTheme = false))
    }

    @Test
    fun lightAndDarkModesIgnoreTheSystem() {
        assertFalse(success(ThemeMode.LIGHT).shouldUseDarkTheme(isSystemInDarkTheme = true))
        assertTrue(success(ThemeMode.DARK).shouldUseDarkTheme(isSystemInDarkTheme = false))
    }

    @Test
    fun dynamicColorIsOffUntilPreferencesAreReadAndEnabled() {
        assertFalse(MainUiState.Loading.shouldUseDynamicColor())
        assertFalse(success(ThemeMode.SYSTEM, dynamic = false).shouldUseDynamicColor())
        assertEquals(true, success(ThemeMode.SYSTEM, dynamic = true).shouldUseDynamicColor())
    }
}
