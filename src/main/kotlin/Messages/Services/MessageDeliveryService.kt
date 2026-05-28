package Messages.Services

import Chats.Services.ChatAccessService
import Media.DTO.MediaFile
import Media.Enums.MediaTypeEnum
import Media.Repositories.MessageAttachmentRepository
import Media.Services.MediaService
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
    private val mediaService = MediaService(environment)
    private val attachmentRepository = MessageAttachmentRepository()

    suspend fun createForChat(
        chatId: Int,
        senderUserId: Int,
        value: String,
        type: String,
        mediaFileIds: List<Int> = emptyList(),
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
        val attachments = mediaService.findActiveByIds(mediaFileIds.distinct())
        if (mediaFileIds.isNotEmpty() && attachments.size != mediaFileIds.distinct().size) {
            throw MessageDeliveryException("MEDIA_NOT_FOUND", "One or more media files were not found")
        }
        if (attachments.any { it.uploaderUserId != senderUserId }) {
            throw MessageDeliveryException("MEDIA_ACCESS_DENIED", "Media file is not available")
        }
        if (normalizedValue.isBlank() && attachments.isEmpty()) {
            throw MessageDeliveryException("MESSAGE_CONTENT_REQUIRED", "Message text or media is required")
        }

        val messageType = resolveMessageType(type, attachments)
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

        attachments.forEach { media ->
            attachmentRepository.create(newId, media.id ?: return@forEach)
        }

        val message = messageService.findById(newId)
            ?: throw MessageDeliveryException("MESSAGE_NOT_FOUND", "Created message was not found")

        val sender = userService.findById(senderUserId)?.toEndpointDTO()
        val endpointAttachments = attachments.map { it.toEndpointDTO() }
        val endpointMessage = message.toEndpointDTO(
            member = member,
            sender = sender,
            currentUserId = senderUserId,
            attachments = endpointAttachments
        ) ?: throw MessageDeliveryException("MESSAGE_MAPPING_FAILED", "Created message could not be mapped")

        return CreatedMessageResult(
            message = message,
            member = member,
            sender = sender,
            endpointMessage = endpointMessage,
            attachments = endpointAttachments
        )
    }

    private fun resolveMessageType(type: String, attachments: List<MediaFile>): MessageTypeEnum? {
        if (attachments.isEmpty() || type.lowercase() != MessageTypeEnum.TEXT.string) {
            return MessageTypeEnum.getEnumByType(type.lowercase())
        }

        return when (attachments.firstOrNull()?.mediaType) {
            MediaTypeEnum.PHOTO.value -> MessageTypeEnum.PHOTO
            MediaTypeEnum.VIDEO.value -> MessageTypeEnum.VIDEO
            MediaTypeEnum.DOCUMENT.value -> MessageTypeEnum.FILE
            else -> MessageTypeEnum.TEXT
        }
    }
}

class MessageDeliveryException(
    val code: String,
    override val message: String
) : RuntimeException(message)
