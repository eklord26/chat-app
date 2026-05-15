package Encryption.DTO

import Messages.Enum.MessageTypeEnum
import kotlinx.serialization.Serializable

@Serializable
data class EncryptedSocketMessage(
    val idChat: Int,
    val idChatMember: Int,
    val value: String,
    val type: MessageTypeEnum? = null,
    val isEncrypted: Boolean = true,
    val encryptionAlgorithm: String = "AES-256-GCM",
    val encryptionKeyVersion: Int,
    val encryptionNonce: String
)
