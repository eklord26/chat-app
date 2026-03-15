package Messages.DTO

import Messages.Enum.MessageTypeEnum
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Int,
    val idChatMember: Int,
    val value: String,
    val type: MessageTypeEnum?,
    val createdAt: String,
    val viewedAt: String? = null,
    val deletedAt: String? = null
)