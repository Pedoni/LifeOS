package it.edon.lifeos.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun textStyle(size: Int, lineHeight: Int, weight: FontWeight, letterSpacing: Double) =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp
    )

// System font for now. Sizes are in sp so they follow the user's font scale.
// Headlines and large titles are semi-bold to give hierarchy without color.
internal val LifeOSTypography = Typography(
    displayLarge = textStyle(57, 64, FontWeight.Normal, -0.25),
    displayMedium = textStyle(45, 52, FontWeight.Normal, 0.0),
    displaySmall = textStyle(36, 44, FontWeight.Normal, 0.0),
    headlineLarge = textStyle(32, 40, FontWeight.SemiBold, 0.0),
    headlineMedium = textStyle(28, 36, FontWeight.SemiBold, 0.0),
    headlineSmall = textStyle(24, 32, FontWeight.SemiBold, 0.0),
    titleLarge = textStyle(22, 28, FontWeight.SemiBold, 0.0),
    titleMedium = textStyle(16, 24, FontWeight.Medium, 0.15),
    titleSmall = textStyle(14, 20, FontWeight.Medium, 0.1),
    bodyLarge = textStyle(16, 24, FontWeight.Normal, 0.5),
    bodyMedium = textStyle(14, 20, FontWeight.Normal, 0.25),
    bodySmall = textStyle(12, 16, FontWeight.Normal, 0.4),
    labelLarge = textStyle(14, 20, FontWeight.Medium, 0.1),
    labelMedium = textStyle(12, 16, FontWeight.Medium, 0.5),
    labelSmall = textStyle(11, 16, FontWeight.Medium, 0.5)
)
