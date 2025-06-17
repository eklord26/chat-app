package Messages.DTO

import Messages.Enum.MessageTypeEnum
import kotlinx.serialization.Serializable
import java.awt.TrayIcon

@Serializable
data class Message (
    val id: Int,
    val idChatMember: Int,
    val value: String,
    val type: MessageTypeEnum?,
    val createdAt: String,
    val viewedAt: String,
    val deletedAt: String,
    val deleted: Boolean)
