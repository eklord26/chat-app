package Encryption.DTO

import kotlinx.serialization.Serializable

@Serializable
data class EncryptedPayload(
    val cipherText: String,
    val nonce: String,
    val algorithm: String = "AES-256-GCM",
    val keyVersion: Int = 1
)
