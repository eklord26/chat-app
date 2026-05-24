package Messages.Services

import Chats.Services.ChatAccessService
import Messages.DTO.CreatedMessageResult
import Messages.DTO.Message
import Messages.Enum.MessageTypeEnum
import Web.Endpoints.toEndpointDTO
import com.example.Users.Services.UserService
import io.ktor.server.application.ApplicationEnvironment
import java.time.Instant

class MessageDeliveryService(environment: ApplicationEnvironment? = null) {
    private val chatAccessService = ChatAccessService(environment)
    private val messageService = MessageService()
    private val userService = UserService(environment)

    suspend fun createForChat(
        chatId: Int,
        senderUserId: Int,
        value: String,
        type: String,
        isEncrypted: Boolean = false,
        encryptionAlgorithm: String? = null,
        encryptionKeyVersion: Int? = null,
        encryptionNonce: String? = null
    ): CreatedMessageResult {
        val member = chatAccessService.requireActiveMember(chatId, senderUserId)
            ?: throw MessageDeliveryException("CHAT_ACCESS_DENIED", "Chat is not available")

        val memberId = member.id
            ?: throw MessageDeliveryException("INVALID_CHAT_MEMBER", "Chat member has no ID")

        val normalizedValue = value.trim()
        if (normalizedValue.isBlank()) {
            throw MessageDeliveryException("MESSAGE_VALUE_REQUIRED", "Message text is required")
        }

        val messageType = MessageTypeEnum.getEnumByType(type.lowercase())
            ?: throw MessageDeliveryException("UNSUPPORTED_MESSAGE_TYPE", "Unsupported message type")

        val newId = messageService.create(
            Message(
                idChatMember = memberId,
                value = normalizedValue,
                type = messageType,
                isEncrypted = isEncrypted,
                encryptionAlgorithm = encryptionAlgorithm,
                encryptionKeyVersion = encryptionKeyVersion,
                encryptionNonce = encryptionNonce,
                createdAt = Instant.now().toString()
            )
        ) ?: throw MessageDeliveryException("MESSAGE_CREATE_FAILED", "Message was not created")

        val message = messageService.findById(newId)
            ?: throw MessageDeliveryException("MESSAGE_NOT_FOUND", "Created message was not found")

        val sender = userService.findById(senderUserId)?.toEndpointDTO()
        val endpointMessage = message.toEndpointDTO(
            member = member,
            sender = sender,
            currentUserId = senderUserId
        ) ?: throw MessageDeliveryException("MESSAGE_MAPPING_FAILED", "Created message could not be mapped")

        return CreatedMessageResult(
            message = message,
            member = member,
            sender = sender,
            endpointMessage = endpointMessage
        )
    }
}

class MessageDeliveryException(
    val code: String,
    override val message: String
) : RuntimeException(message)
