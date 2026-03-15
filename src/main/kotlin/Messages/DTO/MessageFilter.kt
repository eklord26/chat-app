package Messages.DTO

import Messages.Enum.MessageTypeEnum
import kotlinx.serialization.Serializable

@Serializable
data class MessageFilter(
    val value: String? = null,
    val type: MessageTypeEnum? = null,
    val idChatMember: Int? = null,
    val isDeleted: Boolean? = null,
    val createdAt: String? = null,
    val viewedAt: String? = null
)