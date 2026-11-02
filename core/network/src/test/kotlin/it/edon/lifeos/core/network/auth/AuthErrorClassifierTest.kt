package it.edon.lifeos.core.network.auth

import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthErrorClassifierTest {
    @Test
    fun wrongCredentials() {
        assertTrue(
            classifyAuthError("invalid_credentials", 400) is RemoteAuthException.InvalidCredentials
        )
        assertTrue(
            classifyAuthError("invalid_grant", 400) is RemoteAuthException.InvalidCredentials
        )
    }

    @Test
    fun existingAccount() {
        assertTrue(
            classifyAuthError(
                "user_already_exists",
                422
            ) is RemoteAuthException.EmailAlreadyRegistered
        )
        assertTrue(
            classifyAuthError("email_exists", 422) is RemoteAuthException.EmailAlreadyRegistered
        )
    }

    @Test
    fun weakPassword() {
        assertTrue(classifyAuthError("weak_password", 422) is RemoteAuthException.WeakPassword)
    }

    @Test
    fun unconfirmedEmail() {
        assertTrue(
            classifyAuthError("email_not_confirmed", 400) is RemoteAuthException.EmailNotConfirmed
        )
    }

    @Test
    fun rateLimitByCodeOrByStatus() {
        assertTrue(
            classifyAuthError("over_email_send_rate_limit", 429) is RemoteAuthException.RateLimited
        )
        assertTrue(classifyAuthError(null, 429) is RemoteAuthException.RateLimited)
    }

    @Test
    fun invalidInput() {
        assertTrue(
            classifyAuthError("email_address_invalid", 400) is RemoteAuthException.InvalidInput
        )
        assertTrue(classifyAuthError("validation_failed", 400) is RemoteAuthException.InvalidInput)
    }

    @Test
    fun anythingElseIsUnknownAndKeepsTheCode() {
        val error = classifyAuthError("something_new", 500)

        assertTrue(error is RemoteAuthException.Unknown)
        assertTrue((error as RemoteAuthException.Unknown).code == "something_new")
    }

    @Test
    fun theOriginalExceptionIsKeptAsTheCause() {
        val original = IllegalStateException("original")

        assertSame(original, classifyAuthError("invalid_credentials", 400, original).cause)
        assertSame(original, classifyAuthError("something_new", 500, original).cause)
    }
}
