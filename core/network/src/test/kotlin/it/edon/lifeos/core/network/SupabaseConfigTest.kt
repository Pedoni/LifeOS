package it.edon.lifeos.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class SupabaseConfigTest {
    @Test
    fun keepsTheValues() {
        val config = SupabaseConfig("https://abc.supabase.co", "public-key")

        assertEquals("https://abc.supabase.co", config.url)
        assertEquals("public-key", config.anonKey)
    }

    @Test
    fun rejectsABlankUrl() {
        assertThrows(IllegalArgumentException::class.java) { SupabaseConfig(" ", "key") }
    }

    @Test
    fun rejectsABlankKey() {
        assertThrows(IllegalArgumentException::class.java) { SupabaseConfig("https://x", "") }
    }

    @Test
    fun theKeyNeverAppearsInTheStringForm() {
        val text = SupabaseConfig("https://x.supabase.co", "super-secret-key").toString()

        assertFalse(text.contains("super-secret-key"))
    }
}
