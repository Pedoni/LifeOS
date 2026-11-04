package it.edon.lifeos.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import it.edon.lifeos.R
import it.edon.lifeos.core.designsystem.component.LifeOSCard
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.navigation.TopLevelDestination

/**
 * Hub of the sections that do not have a tab of their own. An entry appears here only when its
 * feature exists.
 */
@Composable
fun MoreScreen(onOpenProfile: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(LifeOSTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.md)
    ) {
        Text(
            text = stringResource(TopLevelDestination.More.labelRes),
            style = LifeOSTheme.typography.headlineMedium,
            color = LifeOSTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        LifeOSCard(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.more_profile),
                style = LifeOSTheme.typography.titleMedium,
                color = LifeOSTheme.colors.onSurface
            )
        }
    }
}
