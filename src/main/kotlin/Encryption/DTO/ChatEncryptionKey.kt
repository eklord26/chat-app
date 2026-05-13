package Encryption.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatEncryptionKey(
    val id: Int? = null,
    val idChat: Int,
    val keyCipherText: String,
    val nonce: String,
    val algorithm: String,
    val version: Int,
    val createdAt: String,
    val rotatedAt: String? = null,
    val revokedAt: String? = null
)
