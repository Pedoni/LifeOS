package it.edon.lifeos.core.network.session

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.security.GeneralSecurityException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeystoreSessionCipherTest {
    private val cipher = KeystoreSessionCipher()

    @Test
    fun whatIsEncryptedCanBeDecrypted() {
        val plain = "session-payload".toByteArray()

        assertArrayEquals(plain, cipher.decrypt(cipher.encrypt(plain)))
    }

    @Test
    fun theCiphertextDoesNotContainThePlainText() {
        val plain = "session-payload".toByteArray()

        assertFalse(String(cipher.encrypt(plain)).contains("session-payload"))
    }

    @Test
    fun encryptingTwiceGivesDifferentOutput() {
        val plain = "session-payload".toByteArray()

        assertFalse(cipher.encrypt(plain).contentEquals(cipher.encrypt(plain)))
    }

    @Test
    fun tamperedDataIsRejected() {
        val encrypted = cipher.encrypt("session-payload".toByteArray())
        encrypted[encrypted.size - 1] = (encrypted.last().toInt() xor 1).toByte()

        assertThrows(GeneralSecurityException::class.java) { cipher.decrypt(encrypted) }
    }
}
