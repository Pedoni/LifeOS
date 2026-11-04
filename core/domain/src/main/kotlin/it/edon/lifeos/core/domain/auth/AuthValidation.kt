package it.edon.lifeos.core.domain.auth

enum class EmailIssue { BLANK, INVALID }

enum class PasswordIssue { BLANK, TOO_SHORT }

enum class DisplayNameIssue { TOO_LONG }

/**
 * Rules for what a person may enter when signing up or in. The backend enforces its own limits
 * too (the minimum password length must be set to the same value in the Supabase dashboard);
 * these rules give a clear answer before anything is sent.
 */
object AuthValidation {
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_DISPLAY_NAME_LENGTH = 100

    private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun emailIssue(email: String): EmailIssue? = when {
        email.isBlank() -> EmailIssue.BLANK
        !EMAIL_PATTERN.matches(email.trim()) -> EmailIssue.INVALID
        else -> null
    }

    // Only the length counts: no composition rules, and the password is never trimmed.
    fun passwordIssue(password: String): PasswordIssue? = when {
        password.isEmpty() -> PasswordIssue.BLANK
        password.length < MIN_PASSWORD_LENGTH -> PasswordIssue.TOO_SHORT
        else -> null
    }

    fun displayNameIssue(displayName: String): DisplayNameIssue? =
        if (displayName.trim().length > MAX_DISPLAY_NAME_LENGTH) DisplayNameIssue.TOO_LONG else null
}
