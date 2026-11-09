package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class MessageEndpointDTO(
    val id: Int,
    val idChatMember: Int,
    val idChat: Int,
    val senderUserId: Int,
    val sender: UserEndpointDTO? = null,
    val value: String,
    val type: String,
    val isMine: Boolean,
    val isUnread: Boolean = false,
    val attachments: List<MediaFileEndpointDTO> = emptyList(),
    val isEncrypted: Boolean = false,
    val encryptionAlgorithm: String? = null,
    val encryptionKeyVersion: Int? = null,
    val encryptionNonce: String? = null,
    val createdAt: String,
    val viewedAt: String? = null,
    val deletedAt: String? = null
)
