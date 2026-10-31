package it.edon.lifeos.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ComponentsPreview() {
    LifeOSTheme {
        Surface {
            Column(
                modifier = Modifier.padding(LifeOSTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.lg)
            ) {
                Text(text = "Headline", style = LifeOSTheme.typography.headlineSmall)
                LifeOSButton(text = "Primary action", onClick = {})
                LifeOSTextButton(text = "Secondary action", onClick = {})
                LifeOSTextField(value = "Buy milk", onValueChange = {}, label = "Title")
                LifeOSTextField(
                    value = "",
                    onValueChange = {},
                    label = "Title",
                    isError = true,
                    supportingText = "A title is required"
                )
                LifeOSCard {
                    Text(text = "Project", style = LifeOSTheme.typography.titleMedium)
                    LifeOSLinearProgress(progress = 0.6f)
                }
            }
        }
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun EmptyStatePreview() {
    LifeOSTheme {
        LifeOSEmptyState(
            title = "No tasks yet",
            message = "Create your first task to get started.",
            actionLabel = "New task",
            onAction = {}
        )
    }
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun ErrorStatePreview() {
    LifeOSTheme {
        LifeOSErrorState(
            title = "Something went wrong",
            message = "We could not load your tasks.",
            retryLabel = "Try again",
            onRetry = {}
        )
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun LoadingStatePreview() {
    LifeOSTheme {
        LifeOSLoadingState(message = "Loading")
    }
}
