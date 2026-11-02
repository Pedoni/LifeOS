package it.edon.lifeos.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/**
 * Shared frame of the authentication screens: a title, an optional subtitle and the content,
 * kept clear of the system bars and the keyboard, scrollable for small screens and large fonts,
 * and limited in width on tablets.
 */
@Composable
internal fun AuthFormLayout(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(LifeOSTheme.spacing.xl),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = LifeOSTheme.spacing.formMaxWidth)
                .padding(top = LifeOSTheme.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.lg)
        ) {
            Text(
                text = title,
                style = LifeOSTheme.typography.headlineMedium,
                color = LifeOSTheme.colors.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = LifeOSTheme.typography.bodyLarge,
                    color = LifeOSTheme.colors.onSurfaceVariant
                )
            }
            content()
        }
    }
}
