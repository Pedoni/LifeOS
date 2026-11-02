package it.edon.lifeos.feature.auth.signin

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.edon.lifeos.core.designsystem.component.LifeOSButton
import it.edon.lifeos.core.designsystem.component.LifeOSErrorBanner
import it.edon.lifeos.core.designsystem.component.LifeOSPasswordField
import it.edon.lifeos.core.designsystem.component.LifeOSTextButton
import it.edon.lifeos.core.designsystem.component.LifeOSTextField
import it.edon.lifeos.core.designsystem.theme.LifeOSTheme
import it.edon.lifeos.core.domain.model.AuthError
import it.edon.lifeos.feature.auth.AuthFormLayout
import it.edon.lifeos.feature.auth.R
import it.edon.lifeos.feature.auth.message
import it.edon.lifeos.feature.auth.messageRes

@Composable
fun SignInScreen(
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SignInContent(
        state = state,
        onEvent = viewModel::onEvent,
        onCreateAccount = onCreateAccount,
        modifier = modifier
    )
}

@Composable
internal fun SignInContent(
    state: SignInUiState,
    onEvent: (SignInEvent) -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthFormLayout(
        title = stringResource(R.string.auth_sign_in_title),
        subtitle = stringResource(R.string.auth_sign_in_subtitle),
        modifier = modifier
    ) {
        LifeOSTextField(
            value = state.email,
            onValueChange = { onEvent(SignInEvent.EmailChanged(it)) },
            label = stringResource(R.string.auth_field_email),
            enabled = !state.isSubmitting,
            isError = state.emailIssue != null,
            supportingText = state.emailIssue?.message(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            contentType = ContentType.EmailAddress,
            modifier = Modifier.fillMaxWidth()
        )
        LifeOSPasswordField(
            value = state.password,
            onValueChange = { onEvent(SignInEvent.PasswordChanged(it)) },
            label = stringResource(R.string.auth_field_password),
            isPasswordVisible = state.isPasswordVisible,
            onPasswordVisibilityChange = { onEvent(SignInEvent.TogglePasswordVisibility) },
            showPasswordDescription = stringResource(R.string.auth_show_password),
            hidePasswordDescription = stringResource(R.string.auth_hide_password),
            enabled = !state.isSubmitting,
            isError = state.passwordIssue != null,
            supportingText = state.passwordIssue?.message(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onEvent(SignInEvent.Submit) }),
            modifier = Modifier.fillMaxWidth()
        )
        state.error?.let { LifeOSErrorBanner(message = stringResource(it.messageRes())) }
        LifeOSButton(
            text = stringResource(R.string.auth_sign_in_action),
            onClick = { onEvent(SignInEvent.Submit) },
            isLoading = state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        LifeOSTextButton(
            text = stringResource(R.string.auth_go_to_sign_up),
            onClick = onCreateAccount,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SignInPreview() {
    LifeOSTheme {
        Surface {
            SignInContent(
                state = SignInUiState(
                    email = "ada@example.com",
                    error = AuthError.InvalidCredentials
                ),
                onEvent = {},
                onCreateAccount = {}
            )
        }
    }
}
