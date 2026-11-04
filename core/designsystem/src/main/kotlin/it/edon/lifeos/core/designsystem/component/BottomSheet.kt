package it.edon.lifeos.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme

/** Panel from the bottom edge for options that do not deserve a screen. It scrolls if it is tall. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeOSBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LifeOSTheme.spacing.xl)
                .padding(bottom = LifeOSTheme.spacing.xl)
                .navigationBarsPadding(),
            content = content
        )
    }
}
