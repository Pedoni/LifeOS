package it.edon.lifeos.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OutcomeTest {
    private val success: Outcome<Int, String> = Outcome.Success(2)
    private val failure: Outcome<Int, String> = Outcome.Failure("boom")

    @Test
    fun mapTransformsASuccess() {
        assertEquals(Outcome.Success(4), success.map { it * 2 })
    }

    @Test
    fun mapKeepsAFailureUntouched() {
        assertEquals(Outcome.Failure("boom"), failure.map { it * 2 })
    }

    @Test
    fun getOrNullReturnsTheValueOnlyForASuccess() {
        assertEquals(2, success.getOrNull())
        assertNull(failure.getOrNull())
    }

    @Test
    fun errorOrNullReturnsTheErrorOnlyForAFailure() {
        assertEquals("boom", failure.errorOrNull())
        assertNull(success.errorOrNull())
    }
}
