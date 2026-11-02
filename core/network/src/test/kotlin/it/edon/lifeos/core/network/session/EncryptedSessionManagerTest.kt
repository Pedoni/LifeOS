package it.edon.lifeos.core.network.session

import io.github.jan.supabase.auth.user.UserSession
import java.io.File
import java.security.GeneralSecurityException
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Not real encryption: it only has to be reversible and visibly different from the input. */
private class ReversibleCipher : SessionCipher {
    override fun encrypt(plain: ByteArray) =
        plain.map { (it.toInt() xor KEY).toByte() }.toByteArray()

    override fun decrypt(encrypted: ByteArray) = encrypt(encrypted)

    private companion object {
        const val KEY = 0x5A
    }
}

private class BrokenCipher : SessionCipher {
    override fun encrypt(plain: ByteArray) = plain

    override fun decrypt(encrypted: ByteArray): ByteArray =
        throw GeneralSecurityException("The key is gone")
}

// UserSession.expiresAt is a kotlin.time.Instant, which is still experimental.
@OptIn(ExperimentalTime::class)
class EncryptedSessionManagerTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val sessionFile get() = File(folder.root, "auth/session.bin")

    private fun TestScope.manager(cipher: SessionCipher = ReversibleCipher()) =
        EncryptedSessionManager(sessionFile, cipher, StandardTestDispatcher(testScheduler))

    private fun session(token: String = "access") = UserSession(
        accessToken = token,
        refreshToken = "refresh",
        expiresIn = 3600,
        tokenType = "bearer"
    )

    @Test
    fun aSavedSessionIsLoadedBackIntact() = runTest {
        val manager = manager()
        val saved = session()

        manager.saveSession(saved)

        assertEquals(saved, manager.loadSession())
    }

    @Test
    fun theExpiryDateIsKeptNotRecomputed() = runTest {
        val manager = manager()
        val saved = session()
        manager.saveSession(saved)
        // The expiry defaults to "now + expiresIn": make sure the clock has moved on before
        // reading, so a recomputed value would differ from the saved one.
        Thread.sleep(CLOCK_TICK_MILLIS)

        assertEquals(saved.expiresAt, manager.loadSession().expiresAt)
    }

    @Test
    fun theTokenIsNotWrittenInThePlain() = runTest {
        manager().saveSession(session(token = "very-secret-token"))

        assertFalse(String(sessionFile.readBytes()).contains("very-secret-token"))
    }

    @Test
    fun savingAgainReplacesTheSession() = runTest {
        val manager = manager()
        manager.saveSession(session("first"))

        manager.saveSession(session("second"))

        assertEquals("second", manager.loadSession().accessToken)
    }

    @Test
    fun noSessionMeansNothingToLoad() = runTest {
        val manager = manager()

        assertTrue(runCatching { manager.loadSession() }.exceptionOrNull() is IllegalStateException)
        assertNull(manager.loadSessionOrNull())
    }

    @Test
    fun deletingRemovesTheSession() = runTest {
        val manager = manager()
        manager.saveSession(session())

        manager.deleteSession()

        assertFalse(sessionFile.exists())
        assertNull(manager.loadSessionOrNull())
    }

    @Test
    fun aSessionThatCannotBeDecryptedIsDiscarded() = runTest {
        manager().saveSession(session())

        assertNull(manager(BrokenCipher()).loadSessionOrNull())
        assertFalse(sessionFile.exists())
    }

    @Test
    fun aCorruptedFileIsDiscarded() = runTest {
        val manager = manager()
        manager.saveSession(session())
        sessionFile.writeBytes(ReversibleCipher().encrypt("not json".toByteArray()))

        assertNull(manager.loadSessionOrNull())
        assertFalse(sessionFile.exists())
    }

    private companion object {
        const val CLOCK_TICK_MILLIS = 20L
    }
}
