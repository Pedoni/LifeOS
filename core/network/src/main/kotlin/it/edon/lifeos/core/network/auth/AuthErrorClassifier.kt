package it.edon.lifeos.core.network.auth

private const val HTTP_TOO_MANY_REQUESTS = 429

// "invalid_grant" is what older servers answer to a wrong password.
private val INVALID_CREDENTIALS_CODES = setOf("invalid_credentials", "invalid_grant")
private val EMAIL_EXISTS_CODES = setOf("email_exists", "user_already_exists")
private val RATE_LIMIT_CODES = setOf("over_request_rate_limit", "over_email_send_rate_limit")
private val INVALID_INPUT_CODES = setOf("validation_failed", "email_address_invalid", "bad_json")

/**
 * Turns the error code and HTTP status of a backend answer into one of our failures. Kept free of
 * library types so that it can be tested without building a real response. [cause] is the
 * original exception, kept for diagnosis.
 */
internal fun classifyAuthError(
    code: String?,
    statusCode: Int?,
    cause: Throwable? = null
): RemoteAuthException = when {
    code in INVALID_CREDENTIALS_CODES -> RemoteAuthException.InvalidCredentials(cause)

    code in EMAIL_EXISTS_CODES -> RemoteAuthException.EmailAlreadyRegistered(cause)

    code == "weak_password" -> RemoteAuthException.WeakPassword(cause)

    code == "email_not_confirmed" -> RemoteAuthException.EmailNotConfirmed(cause)

    code in RATE_LIMIT_CODES || statusCode == HTTP_TOO_MANY_REQUESTS ->
        RemoteAuthException.RateLimited(cause)

    code in INVALID_INPUT_CODES -> RemoteAuthException.InvalidInput(cause)

    else -> RemoteAuthException.Unknown(code, cause)
}
