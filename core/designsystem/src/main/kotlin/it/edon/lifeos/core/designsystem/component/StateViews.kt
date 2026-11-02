package it.edon.lifeos.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/** Loading state of a screen. [message] doubles as the spoken description for screen readers. */
@Composable
fun LifeOSLoadingState(modifier: Modifier = Modifier, message: String? = null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.lg)
        ) {
            CircularProgressIndicator()
            if (message != null) {
                Text(
                    text = message,
                    style = LifeOSTheme.typography.bodyMedium,
                    color = LifeOSTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

/** Empty state: explains what is missing and, through [actionLabel], what the user can do next. */
@Composable
fun LifeOSEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    StateLayout(title = title, message = message, modifier = modifier, icon = icon) {
        if (actionLabel != null && onAction != null) {
            LifeOSButton(text = actionLabel, onClick = onAction)
        }
    }
}

/** Error state: says what went wrong in plain words and always offers a way to retry. */
@Composable
fun LifeOSErrorState(
    title: String,
    message: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    StateLayout(title = title, message = message, modifier = modifier, icon = icon) {
        LifeOSButton(text = retryLabel, onClick = onRetry)
    }
}

@Composable
private fun StateLayout(
    title: String,
    message: String,
    modifier: Modifier,
    icon: (@Composable () -> Unit)?,
    action: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(LifeOSTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.md)
        ) {
            icon?.invoke()
            Text(
                text = title,
                style = LifeOSTheme.typography.titleMedium,
                color = LifeOSTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = LifeOSTheme.typography.bodyMedium,
                color = LifeOSTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            action()
        }
    }
}
