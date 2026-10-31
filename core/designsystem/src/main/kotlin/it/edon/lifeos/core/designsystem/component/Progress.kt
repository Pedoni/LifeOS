package it.edon.lifeos.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/**
 * Determinate progress bar for projects, goals and habits. [progress] is a fraction between
 * 0 and 1; values outside the range are clamped.
 */
@Composable
fun LifeOSLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = LifeOSTheme.colors.primary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = LifeOSTheme.motion.durationMedium,
            easing = LifeOSTheme.motion.easing
        ),
        label = "progress"
    )
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier.fillMaxWidth(),
        color = color,
        trackColor = LifeOSTheme.colors.surfaceContainerHighest,
        strokeCap = StrokeCap.Round
    )
}
