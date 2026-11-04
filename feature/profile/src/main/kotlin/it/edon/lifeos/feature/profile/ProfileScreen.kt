package it.edon.lifeos.feature.profile

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.edon.lifeos.core.designsystem.component.LifeOSButton
import it.edon.lifeos.core.designsystem.component.LifeOSConfirmDialog
import it.edon.lifeos.core.designsystem.component.LifeOSErrorBanner
import it.edon.lifeos.core.designsystem.component.LifeOSLoadingState
import it.edon.lifeos.core.designsystem.component.LifeOSRadioRow
import it.edon.lifeos.core.designsystem.component.LifeOSSwitchRow
import it.edon.lifeos.core.designsystem.component.LifeOSTextButton
import it.edon.lifeos.core.designsystem.component.LifeOSTextField
import it.edon.lifeos.core.designsystem.component.LifeOSTopBar
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.auth.AuthValidation
import it.edon.lifeos.core.domain.auth.DisplayNameIssue
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.core.domain.model.AuthUser
import it.edon.lifeos.core.domain.model.ThemeMode
import it.edon.lifeos.core.domain.model.UserPreferences

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
internal fun ProfileContent(
    state: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            LifeOSTopBar(
                title = stringResource(R.string.profile_title),
                onBack = onBack,
                backDescription = stringResource(R.string.profile_back)
            )
        }
    ) { innerPadding ->
        when (state) {
            ProfileUiState.Loading -> LifeOSLoadingState(Modifier.padding(innerPadding))

            is ProfileUiState.Ready -> ReadyContent(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ReadyContent(
    state: ProfileUiState.Ready,
    onEvent: (ProfileEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LifeOSTheme.spacing.xl),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.widthIn(max = LifeOSTheme.spacing.formMaxWidth),
            verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.xl)
        ) {
            state.error?.let { LifeOSErrorBanner(message = stringResource(it.messageRes())) }
            AccountSection(state, onEvent)
            AppearanceSection(state, onEvent)
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.sm)) {
                LifeOSButton(
                    text = stringResource(R.string.profile_sign_out),
                    onClick = { onEvent(ProfileEvent.SignOut) },
                    enabled = !state.isBusy,
                    modifier = Modifier.fillMaxWidth()
                )
                LifeOSTextButton(
                    text = stringResource(R.string.profile_delete_account),
                    onClick = { onEvent(ProfileEvent.RequestAccountDeletion) },
                    enabled = !state.isBusy,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    if (state.isDeleteDialogVisible) {
        LifeOSConfirmDialog(
            title = stringResource(R.string.profile_delete_title),
            message = stringResource(R.string.profile_delete_message),
            confirmLabel = stringResource(R.string.profile_delete_confirm),
            dismissLabel = stringResource(R.string.profile_delete_cancel),
            onConfirm = { onEvent(ProfileEvent.ConfirmAccountDeletion) },
            onDismiss = { onEvent(ProfileEvent.DismissAccountDeletion) },
            isDestructive = true,
            isBusy = state.isBusy
        )
    }
}

@Composable
private fun AccountSection(state: ProfileUiState.Ready, onEvent: (ProfileEvent) -> Unit) {
    Section(title = stringResource(R.string.profile_section_account)) {
        state.user.email?.let { email ->
            Text(
                text = stringResource(R.string.profile_email),
                style = LifeOSTheme.typography.labelMedium,
                color = LifeOSTheme.colors.onSurfaceVariant
            )
            Text(
                text = email,
                style = LifeOSTheme.typography.bodyLarge,
                color = LifeOSTheme.colors.onSurface
            )
        }
        LifeOSTextField(
            value = state.displayName,
            onValueChange = { onEvent(ProfileEvent.DisplayNameChanged(it)) },
            label = stringResource(R.string.profile_field_display_name),
            enabled = !state.isBusy,
            isError = state.displayNameIssue != null,
            supportingText = state.displayNameIssue?.message(),
            modifier = Modifier.fillMaxWidth()
        )
        LifeOSButton(
            text = stringResource(R.string.profile_save_name),
            onClick = { onEvent(ProfileEvent.SaveDisplayName) },
            enabled = state.canSaveDisplayName && !state.isBusy,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AppearanceSection(state: ProfileUiState.Ready, onEvent: (ProfileEvent) -> Unit) {
    Section(title = stringResource(R.string.profile_section_appearance)) {
        ThemeMode.entries.forEach { mode ->
            LifeOSRadioRow(
                label = stringResource(mode.labelRes()),
                selected = state.preferences.themeMode == mode,
                onClick = { onEvent(ProfileEvent.ThemeModeSelected(mode)) }
            )
        }
        // Wallpaper colors exist from Android 12; before that the switch would do nothing.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            LifeOSSwitchRow(
                label = stringResource(R.string.profile_dynamic_color),
                supportingText = stringResource(R.string.profile_dynamic_color_description),
                checked = state.preferences.useDynamicColor,
                onCheckedChange = { onEvent(ProfileEvent.DynamicColorChanged(it)) }
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(LifeOSTheme.spacing.md)) {
        Text(
            text = title,
            style = LifeOSTheme.typography.titleMedium,
            color = LifeOSTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        content()
    }
}

@Composable
private fun DisplayNameIssue.message(): String = when (this) {
    DisplayNameIssue.TOO_LONG -> stringResource(
        R.string.profile_issue_display_name_too_long,
        AuthValidation.MAX_DISPLAY_NAME_LENGTH
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ProfilePreview() {
    LifeOSTheme {
        Surface {
            ProfileContent(
                state = ProfileUiState.Ready(
                    user = AuthUser("u1", "ada@example.com", "Ada"),
                    preferences = UserPreferences(themeMode = ThemeMode.DARK),
                    displayName = "Ada",
                    canSaveDisplayName = false,
                    error = AuthError.Network
                ),
                onEvent = {},
                onBack = {}
            )
        }
    }
}
