package Encryption.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatEncryptionKeysResponse(
    val idChat: Int,
    val keys: List<ChatEncryptionKeyResponse>
)
