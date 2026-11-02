package it.edon.lifeos.core.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Local, per-device application preferences. */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false
)
