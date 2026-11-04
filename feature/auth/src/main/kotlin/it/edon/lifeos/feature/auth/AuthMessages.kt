package it.edon.lifeos.feature.auth

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.edon.lifeos.core.domain.auth.AuthValidation
import it.edon.lifeos.core.domain.auth.DisplayNameIssue
import it.edon.lifeos.core.domain.auth.EmailIssue
import it.edon.lifeos.core.domain.auth.PasswordIssue
import it.edon.lifeos.core.domain.model.AuthError

@StringRes
internal fun AuthError.messageRes(): Int = when (this) {
    AuthError.InvalidCredentials -> R.string.auth_error_invalid_credentials
    AuthError.EmailAlreadyRegistered -> R.string.auth_error_email_registered
    AuthError.WeakPassword -> R.string.auth_error_weak_password
    AuthError.EmailNotConfirmed -> R.string.auth_error_email_not_confirmed
    AuthError.InvalidInput -> R.string.auth_error_invalid_input
    AuthError.RateLimited -> R.string.auth_error_rate_limited
    AuthError.Network -> R.string.auth_error_network
    AuthError.Unknown -> R.string.auth_error_unknown
}

@Composable
internal fun EmailIssue.message(): String = stringResource(
    when (this) {
        EmailIssue.BLANK -> R.string.auth_issue_email_blank
        EmailIssue.INVALID -> R.string.auth_issue_email_invalid
    }
)

@Composable
internal fun PasswordIssue.message(): String = when (this) {
    PasswordIssue.BLANK -> stringResource(R.string.auth_issue_password_blank)

    PasswordIssue.TOO_SHORT ->
        stringResource(R.string.auth_issue_password_too_short, AuthValidation.MIN_PASSWORD_LENGTH)
}

@Composable
internal fun DisplayNameIssue.message(): String = when (this) {
    DisplayNameIssue.TOO_LONG -> stringResource(
        R.string.auth_issue_display_name_too_long,
        AuthValidation.MAX_DISPLAY_NAME_LENGTH
    )
}
