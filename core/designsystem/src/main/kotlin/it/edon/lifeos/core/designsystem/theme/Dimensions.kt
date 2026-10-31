package it.edon.lifeos.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale. Screens use these values instead of literal dp. */
@Immutable
data class LifeOSSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
    /** Minimum size of any tappable element. */
    val minTouchTarget: Dp = 48.dp
)

/**
 * Elevations. Hierarchy is expressed mostly with tonal surfaces rather than shadows,
 * so most components use [none] or [low].
 */
@Immutable
data class LifeOSElevation(
    val none: Dp = 0.dp,
    val low: Dp = 1.dp,
    val medium: Dp = 3.dp,
    val high: Dp = 6.dp
)

/** Animation guidelines: short for feedback, medium for transitions, long for emphasis. */
@Immutable
data class LifeOSMotion(
    val durationShort: Int = 150,
    val durationMedium: Int = 300,
    val durationLong: Int = 500,
    val easing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
)

internal val LocalSpacing = staticCompositionLocalOf { LifeOSSpacing() }
internal val LocalElevation = staticCompositionLocalOf { LifeOSElevation() }
internal val LocalMotion = staticCompositionLocalOf { LifeOSMotion() }
