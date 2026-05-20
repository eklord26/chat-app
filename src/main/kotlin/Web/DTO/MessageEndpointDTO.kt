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
    val createdAt: String,
    val viewedAt: String? = null,
    val deletedAt: String? = null
)
