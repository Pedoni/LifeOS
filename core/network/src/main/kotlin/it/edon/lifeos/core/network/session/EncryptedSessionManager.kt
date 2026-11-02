package it.edon.lifeos.core.network.session

import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.GeneralSecurityException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

// The expiry date has a default value, so defaults must be written or it would be recomputed
// every time the session is read.
private val SessionJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

/**
 * Keeps the authentication session in a single encrypted file.
 *
 * A session that cannot be decrypted or parsed (for example after the Keystore key was lost) is
 * deleted and reported as missing: the user has to sign in again, nothing crashes. Plain I/O
 * errors do not delete anything, since they may be temporary.
 */
internal class EncryptedSessionManager(
    private val file: File,
    private val cipher: SessionCipher,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SessionManager {

    override suspend fun saveSession(session: UserSession) = withContext(ioDispatcher) {
        val plain = SessionJson.encodeToString(UserSession.serializer(), session).toByteArray()
        writeAtomically(cipher.encrypt(plain))
    }

    override suspend fun loadSession(): UserSession = withContext(ioDispatcher) {
        if (!file.exists()) error("No session stored")
        val encrypted = file.readBytes()
        try {
            val plain = cipher.decrypt(encrypted)
            SessionJson.decodeFromString(UserSession.serializer(), String(plain))
        } catch (e: GeneralSecurityException) {
            discardUnreadableSession(e)
        } catch (e: IllegalArgumentException) {
            // Also covers a malformed or truncated payload (SerializationException).
            discardUnreadableSession(e)
        }
    }

    override suspend fun deleteSession() = withContext(ioDispatcher) {
        Files.deleteIfExists(file.toPath())
        Unit
    }

    private fun discardUnreadableSession(cause: Exception): Nothing {
        Files.deleteIfExists(file.toPath())
        throw IllegalStateException(
            "The stored session was unreadable and has been discarded",
            cause
        )
    }

    // Written to a temporary file first, so a crash never leaves half a session behind.
    @Throws(IOException::class)
    private fun writeAtomically(bytes: ByteArray) {
        file.parentFile?.mkdirs()
        val temporary = File(file.parentFile, "${file.name}.tmp")
        temporary.writeBytes(bytes)
        Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}
