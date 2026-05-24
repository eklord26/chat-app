package Sockets

import Messages.DTO.CreatedMessageResult
import Sockets.DTO.SocketErrorDTO
import Sockets.DTO.SocketEventDTO
import Sockets.DTO.SocketMessageDTO
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

object SocketBroadcaster {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun connectionReady(connectionId: String, chatIds: List<Int>) {
        val event = SocketEventDTO(
            event = "connection:ready",
            data = json.encodeToJsonElement(mapOf("chatIds" to chatIds))
        )
        SocketConnectionRegistry.sendToConnection(connectionId, json.encodeToString(event))
    }

    suspend fun messageCreated(result: CreatedMessageResult) {
        val message = result.message
        val messageId = message.id ?: return
        val event = SocketEventDTO(
            event = "message:new",
            data = json.encodeToJsonElement(
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

        SocketConnectionRegistry.broadcastToChat(result.member.idChat, json.encodeToString(event))
    }

    suspend fun error(connectionId: String, code: String, message: String, requestId: String? = null) {
        val event = SocketEventDTO(
            event = "error",
            requestId = requestId,
            data = json.encodeToJsonElement(SocketErrorDTO(code, message))
        )
        SocketConnectionRegistry.sendToConnection(connectionId, json.encodeToString(event))
    }
}
