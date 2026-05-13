package Encryption.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatEncryptionKeyResponse(
    val idChat: Int,
    val key: String,
    val algorithm: String,
    val version: Int,
    val createdAt: String,
    val rotatedAt: String? = null
)
