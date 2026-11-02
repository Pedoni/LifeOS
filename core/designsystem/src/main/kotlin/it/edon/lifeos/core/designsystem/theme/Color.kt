package it.edon.lifeos.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// "Calma" palette: deep indigo as primary, teal as accent (tertiary), cool neutral surfaces.
// Text/background pairs follow the Material 3 tonal roles to keep contrast at AA or better.

internal val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3F51B5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE1FF),
    onPrimaryContainer = Color(0xFF0A1A6B),
    secondary = Color(0xFF5B5F78),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E1FA),
    onSecondaryContainer = Color(0xFF181B2F),
    tertiary = Color(0xFF00796B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB2F0E4),
    onTertiaryContainer = Color(0xFF00201B),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBFBFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFBFBFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF46464F),
    outline = Color(0xFF777680),
    outlineVariant = Color(0xFFC7C5D0),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF303034),
    inverseOnSurface = Color(0xFFF2F0F4),
    inversePrimary = Color(0xFFB8C3FF),
    surfaceTint = Color(0xFF3F51B5),
    surfaceBright = Color(0xFFFBFBFF),
    surfaceDim = Color(0xFFDBD9E0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F3FA),
    surfaceContainer = Color(0xFFEFEDF4),
    surfaceContainerHigh = Color(0xFFE9E7EE),
    surfaceContainerHighest = Color(0xFFE3E1E9)
)

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB8C3FF),
    onPrimary = Color(0xFF08218A),
    primaryContainer = Color(0xFF2A3990),
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFFC4C5DD),
    onSecondary = Color(0xFF2D2F42),
    secondaryContainer = Color(0xFF434659),
    onSecondaryContainer = Color(0xFFE0E1FA),
    tertiary = Color(0xFF7DD8C9),
    onTertiary = Color(0xFF003731),
    tertiaryContainer = Color(0xFF005047),
    onTertiaryContainer = Color(0xFFB2F0E4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121316),
    onBackground = Color(0xFFE4E1E6),
    surface = Color(0xFF121316),
    onSurface = Color(0xFFE4E1E6),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC7C5D0),
    outline = Color(0xFF918F9A),
    outlineVariant = Color(0xFF46464F),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE4E1E6),
    inverseOnSurface = Color(0xFF303034),
    inversePrimary = Color(0xFF3F51B5),
    surfaceTint = Color(0xFFB8C3FF),
    surfaceBright = Color(0xFF38393C),
    surfaceDim = Color(0xFF121316),
    surfaceContainerLowest = Color(0xFF0D0E11),
    surfaceContainerLow = Color(0xFF1B1B1F),
    surfaceContainer = Color(0xFF1F1F23),
    surfaceContainerHigh = Color(0xFF292A2D),
    surfaceContainerHighest = Color(0xFF343438)
)

/**
 * Colors that Material 3 does not provide: the four task priorities and a success color.
 * Priority is never conveyed by color alone; screens must also show a label or icon.
 */
@Immutable
data class LifeOSExtendedColors(
    val priorityLow: Color,
    val priorityMedium: Color,
    val priorityHigh: Color,
    val priorityUrgent: Color,
    val success: Color
)

internal val LightExtendedColors = LifeOSExtendedColors(
    priorityLow = Color(0xFF5B6B7F),
    priorityMedium = Color(0xFF0277BD),
    priorityHigh = Color(0xFFB26A00),
    priorityUrgent = Color(0xFFBA1A1A),
    success = Color(0xFF2E7D32)
)

internal val DarkExtendedColors = LifeOSExtendedColors(
    priorityLow = Color(0xFFA7B4C6),
    priorityMedium = Color(0xFF6FC3F2),
    priorityHigh = Color(0xFFFFB74D),
    priorityUrgent = Color(0xFFFFB4AB),
    success = Color(0xFF81C995)
)

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
