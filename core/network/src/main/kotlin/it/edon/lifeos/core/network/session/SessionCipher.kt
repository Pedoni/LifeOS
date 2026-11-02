package it.edon.lifeos.core.network.session

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "lifeos_session_key"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val KEY_SIZE_BITS = 256
private const val IV_SIZE_BYTES = 12
private const val TAG_SIZE_BITS = 128

/** Encrypts the stored session. Decryption failures throw a `GeneralSecurityException`. */
internal interface SessionCipher {
    fun encrypt(plain: ByteArray): ByteArray

    fun decrypt(encrypted: ByteArray): ByteArray
}

/**
 * AES-256-GCM with a key that lives in the Android Keystore and never leaves it. The output is
 * the random IV followed by the ciphertext. The key is tied to this app installation: if it is
 * lost or invalidated the session cannot be read and the user simply signs in again.
 */
internal class KeystoreSessionCipher : SessionCipher {

    override fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        return cipher.iv + cipher.doFinal(plain)
    }

    override fun decrypt(encrypted: ByteArray): ByteArray {
        require(encrypted.size > IV_SIZE_BYTES) { "Encrypted data is too short" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_SIZE_BITS, encrypted.copyOfRange(0, IV_SIZE_BYTES))
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), spec)
        return cipher.doFinal(encrypted, IV_SIZE_BYTES, encrypted.size - IV_SIZE_BYTES)
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE_BITS)
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }
}
