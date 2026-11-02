package it.edon.lifeos.feature.auth.signup

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
import androidx.compose.ui.text.input.KeyboardCapitalization
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
fun SignUpScreen(
    onBackToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SignUpContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackToSignIn = onBackToSignIn,
        modifier = modifier
    )
}

@Composable
internal fun SignUpContent(
    state: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit,
    onBackToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val confirmationSentTo = state.confirmationSentTo
    if (confirmationSentTo != null) {
        CheckEmailContent(
            email = confirmationSentTo,
            onBackToSignIn = onBackToSignIn,
            modifier = modifier
        )
        return
    }
    AuthFormLayout(
        title = stringResource(R.string.auth_sign_up_title),
        subtitle = stringResource(R.string.auth_sign_up_subtitle),
        modifier = modifier
    ) {
        LifeOSTextField(
            value = state.displayName,
            onValueChange = { onEvent(SignUpEvent.DisplayNameChanged(it)) },
            label = stringResource(R.string.auth_field_display_name),
            enabled = !state.isSubmitting,
            isError = state.displayNameIssue != null,
            supportingText = state.displayNameIssue?.message(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            contentType = ContentType.PersonFullName,
            modifier = Modifier.fillMaxWidth()
        )
        LifeOSTextField(
            value = state.email,
            onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
            label = stringResource(R.string.auth_field_email),
            enabled = !state.isSubmitting,
            isError = state.emailIssue != null,
            supportingText = state.emailIssue?.message(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            contentType = ContentType.NewUsername,
            modifier = Modifier.fillMaxWidth()
        )
        LifeOSPasswordField(
            value = state.password,
            onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
            label = stringResource(R.string.auth_field_password),
            isPasswordVisible = state.isPasswordVisible,
            onPasswordVisibilityChange = { onEvent(SignUpEvent.TogglePasswordVisibility) },
            showPasswordDescription = stringResource(R.string.auth_show_password),
            hidePasswordDescription = stringResource(R.string.auth_hide_password),
            enabled = !state.isSubmitting,
            isError = state.passwordIssue != null,
            supportingText = state.passwordIssue?.message(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onEvent(SignUpEvent.Submit) }),
            contentType = ContentType.NewPassword,
            modifier = Modifier.fillMaxWidth()
        )
        state.error?.let { LifeOSErrorBanner(message = stringResource(it.messageRes())) }
        LifeOSButton(
            text = stringResource(R.string.auth_sign_up_action),
            onClick = { onEvent(SignUpEvent.Submit) },
            isLoading = state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        LifeOSTextButton(
            text = stringResource(R.string.auth_go_to_sign_in),
            onClick = onBackToSignIn,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun CheckEmailContent(
    email: String,
    onBackToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthFormLayout(
        title = stringResource(R.string.auth_check_email_title),
        subtitle = stringResource(R.string.auth_check_email_message, email),
        modifier = modifier
    ) {
        LifeOSButton(
            text = stringResource(R.string.auth_back_to_sign_in),
            onClick = onBackToSignIn,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SignUpPreview() {
    LifeOSTheme {
        Surface {
            SignUpContent(
                state = SignUpUiState(
                    email = "ada@example.com",
                    error = AuthError.EmailAlreadyRegistered
                ),
                onEvent = {},
                onBackToSignIn = {}
            )
        }
    }
}

@Preview(name = "Check email", showBackground = true)
@Composable
private fun CheckEmailPreview() {
    LifeOSTheme {
        Surface {
            CheckEmailContent(email = "ada@example.com", onBackToSignIn = {})
        }
    }
}
