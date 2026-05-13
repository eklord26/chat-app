package Encryption.Services

import Encryption.DTO.EncryptedPayload
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesGcmEncryptionService {
    private val algorithm = "AES-256-GCM"
    private val cipherTransformation = "AES/GCM/NoPadding"
    private val random = SecureRandom()

    fun generateKeyBase64(): String {
        val generator = KeyGenerator.getInstance("AES")
        generator.init(256)
        return Base64.getEncoder().encodeToString(generator.generateKey().encoded)
    }

    fun encrypt(data: String, keyBase64: String, keyVersion: Int = 1): EncryptedPayload {
        val nonce = ByteArray(NONCE_SIZE_BYTES)
        random.nextBytes(nonce)

        val cipher = Cipher.getInstance(cipherTransformation)
        cipher.init(Cipher.ENCRYPT_MODE, decodeKey(keyBase64), GCMParameterSpec(TAG_SIZE_BITS, nonce))

        return EncryptedPayload(
            cipherText = Base64.getEncoder().encodeToString(cipher.doFinal(data.toByteArray(Charsets.UTF_8))),
            nonce = Base64.getEncoder().encodeToString(nonce),
            algorithm = algorithm,
            keyVersion = keyVersion
        )
    }

    fun decrypt(payload: EncryptedPayload, keyBase64: String): String {
        val cipher = Cipher.getInstance(cipherTransformation)
        val nonce = Base64.getDecoder().decode(payload.nonce)
        cipher.init(Cipher.DECRYPT_MODE, decodeKey(keyBase64), GCMParameterSpec(TAG_SIZE_BITS, nonce))

        val plainBytes = cipher.doFinal(Base64.getDecoder().decode(payload.cipherText))
        return String(plainBytes, Charsets.UTF_8)
    }

    private fun decodeKey(keyBase64: String): SecretKey {
        val keyBytes = Base64.getDecoder().decode(keyBase64)
        require(keyBytes.size == KEY_SIZE_BYTES) { "AES-256 key must contain 32 bytes" }
        return SecretKeySpec(keyBytes, "AES")
    }

    private companion object {
        const val KEY_SIZE_BYTES = 32
        const val NONCE_SIZE_BYTES = 12
        const val TAG_SIZE_BITS = 128
    }
}
