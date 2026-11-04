package it.edon.lifeos.core.domain.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {
    @Test
    fun aBlankEmailIsReported() {
        assertEquals(EmailIssue.BLANK, AuthValidation.emailIssue(""))
        assertEquals(EmailIssue.BLANK, AuthValidation.emailIssue("   "))
    }

    @Test
    fun aMalformedEmailIsReported() {
        listOf("ada", "ada@", "@example.com", "ada@example", "a da@example.com", "a@@b.c").forEach {
            assertEquals(it, EmailIssue.INVALID, AuthValidation.emailIssue(it))
        }
    }

    @Test
    fun aWellFormedEmailIsAccepted() {
        assertNull(AuthValidation.emailIssue("ada@example.com"))
        assertNull(AuthValidation.emailIssue("  ada.lovelace+tag@mail.example.org  "))
    }

    @Test
    fun anEmptyPasswordIsReported() {
        assertEquals(PasswordIssue.BLANK, AuthValidation.passwordIssue(""))
    }

    @Test
    fun aShortPasswordIsReported() {
        assertEquals(PasswordIssue.TOO_SHORT, AuthValidation.passwordIssue("1234567"))
    }

    @Test
    fun onlyTheLengthOfAPasswordMatters() {
        assertNull(AuthValidation.passwordIssue("12345678"))
        assertNull(AuthValidation.passwordIssue("        "))
    }

    @Test
    fun aTooLongDisplayNameIsReported() {
        val tooLong = "a".repeat(AuthValidation.MAX_DISPLAY_NAME_LENGTH + 1)

        assertEquals(DisplayNameIssue.TOO_LONG, AuthValidation.displayNameIssue(tooLong))
        assertNull(
            AuthValidation.displayNameIssue("a".repeat(AuthValidation.MAX_DISPLAY_NAME_LENGTH))
        )
        assertNull(AuthValidation.displayNameIssue(""))
    }
}
