package Messages.DTO

import Messages.Enum.MessageTypeEnum
import kotlinx.serialization.Serializable

@Serializable
data class MessageFilter (
    val value: String? = null,
    val type: MessageTypeEnum? = null,
    val idChatMember: Int? = null,
    val createdAt: String? = null,
    val viewedAt: String? = null,
    val deletedAt: String? = null,
    val deleted: Boolean? = null,
)