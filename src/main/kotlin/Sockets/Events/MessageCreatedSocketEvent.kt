package Sockets.Events

import Messages.DTO.CreatedMessageResult
import Sockets.DTO.SocketEventDTO
import Sockets.DTO.SocketMessageDTO
import Sockets.SocketEventContext
import kotlinx.serialization.json.encodeToJsonElement

class MessageCreatedSocketEvent(
    private val result: CreatedMessageResult
) : SocketEvent {
    override suspend fun dispatch(context: SocketEventContext) {
        val message = result.message
        val messageId = message.id ?: return
        val event = SocketEventDTO(
            event = "message:new",
            data = context.json.encodeToJsonElement(
                SocketMessageDTO(
                    id = messageId,
                    idChatMember = result.member.id ?: result.message.idChatMember,
                    idChat = result.member.idChat,
                    senderUserId = result.member.idUser,
                    sender = result.sender,
                    value = message.value,
                    type = message.type?.string ?: "text",
                    isEncrypted = message.isEncrypted,
                    encryptionAlgorithm = message.encryptionAlgorithm,
                    encryptionKeyVersion = message.encryptionKeyVersion,
                    encryptionNonce = message.encryptionNonce,
                    createdAt = message.createdAt,
                    viewedAt = message.viewedAt,
                    deletedAt = message.deletedAt
                )
            )
        )

        context.broadcastToChat(result.member.idChat, event)
    }
}
