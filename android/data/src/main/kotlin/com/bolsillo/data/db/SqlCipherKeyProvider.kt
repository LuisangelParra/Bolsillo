package com.bolsillo.data.db

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps a random 256-bit SQLCipher passphrase with an AES key held in the
 * AndroidKeyStore (research R1 / Articles I/II). The wrapped blob lives in a
 * private file inside the app sandbox; the unwrapped passphrase only lives in
 * memory long enough to open the DB.
 */
@Singleton
class SqlCipherKeyProvider
    @Inject
    constructor(
        private val context: Context,
    ) {
        private val keyAlias = "bolsillo-db-aes"
        private val blobFile by lazy { java.io.File(context.filesDir, "db.key") }
        private val gcmTagLength = 128

        @Synchronized
        fun passphrase(): ByteArray {
            ensureKey()
            return if (blobFile.exists()) unwrap() else wrapAndStore()
        }

        private fun ensureKey() {
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (ks.containsAlias(keyAlias)) return
            val spec =
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                init(spec)
                generateKey()
            }
        }

        private fun secretKey(): SecretKey {
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            return (ks.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
        }

        private fun wrapAndStore(): ByteArray {
            val passphrase = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
            val cipher =
                Cipher.getInstance("AES/GCM/NoPadding").apply {
                    init(Cipher.ENCRYPT_MODE, secretKey())
                }
            val iv = cipher.iv
            val sealed = cipher.doFinal(passphrase)
            blobFile.outputStream().use { out ->
                out.write(iv.size)
                out.write(iv)
                out.write(sealed)
            }
            return passphrase
        }

        private fun unwrap(): ByteArray =
            blobFile.inputStream().use { input ->
                val ivLen = input.read()
                val iv = ByteArray(ivLen).also { input.read(it) }
                val sealed = input.readBytes()
                Cipher.getInstance("AES/GCM/NoPadding").run {
                    init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(gcmTagLength, iv))
                    doFinal(sealed)
                }
            }
    }
