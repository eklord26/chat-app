package Web.Endpoints

import Chats.DTO.Chat
import ChatMembers.DTO.ChatMember
import Contacts.DTO.Contact
import Invitations.DTO.ChatInvitation
import Invitations.DTO.ContactInvitation
import Media.DTO.MediaFile
import Messages.DTO.Message
import Web.DTO.ChatEndpointDTO
import Web.DTO.ChatInvitationEndpointDTO
import Web.DTO.ContactEndpointDTO
import Web.DTO.ContactInvitationEndpointDTO
import Web.DTO.MessageEndpointDTO
import Web.DTO.MediaFileEndpointDTO
import Web.DTO.UserEndpointDTO
import com.example.Users.DTO.User

fun User.toEndpointDTO(): UserEndpointDTO? {
    val userId = id ?: return null
    return UserEndpointDTO(
        id = userId,
        name = name,
        login = login,
        email = email,
        phone = phone,
        fio = fio,
        isAdmin = isAdmin
    )
}

fun Contact.toEndpointDTO(contactUser: UserEndpointDTO?): ContactEndpointDTO? {
    val contactId = id ?: return null
    return ContactEndpointDTO(
        id = contactId,
        ownerUserId = ownerUserId,
        contactUserId = contactUserId,
        displayName = displayName,
        contact = contactUser,
        createdAt = createdAt,
        deletedAt = deletedAt
    )
}

fun Chat.toEndpointDTO(ownerUser: UserEndpointDTO?, currentUserMemberId: Int? = null): ChatEndpointDTO? {
    val chatId = id ?: return null
    return ChatEndpointDTO(
        id = chatId,
        name = name,
        owner = owner,
        ownerUser = ownerUser,
        currentUserMemberId = currentUserMemberId,
        createdAt = createdAt,
        deletedAt = deletedAt
    )
}

fun ContactInvitation.toEndpointDTO(
    sender: UserEndpointDTO?,
    receiver: UserEndpointDTO?
): ContactInvitationEndpointDTO? {
    val invitationId = id ?: return null
    return ContactInvitationEndpointDTO(
        id = invitationId,
        senderUserId = senderUserId,
        receiverUserId = receiverUserId,
        sender = sender,
        receiver = receiver,
        status = status,
        message = message,
        createdAt = createdAt,
        respondedAt = respondedAt,
        deletedAt = deletedAt
    )
}

fun ChatInvitation.toEndpointDTO(
    chat: ChatEndpointDTO?,
    inviter: UserEndpointDTO?,
    invitee: UserEndpointDTO?
): ChatInvitationEndpointDTO? {
    val invitationId = id ?: return null
    return ChatInvitationEndpointDTO(
        id = invitationId,
        idChat = idChat,
        chat = chat,
        inviterUserId = inviterUserId,
        inviteeUserId = inviteeUserId,
        inviter = inviter,
        invitee = invitee,
        idRole = idRole,
        status = status,
        message = message,
        createdAt = createdAt,
        respondedAt = respondedAt,
        deletedAt = deletedAt
    )
}

fun MediaFile.toEndpointDTO(): MediaFileEndpointDTO {
    val mediaId = id ?: error("Media file has no ID")
    return MediaFileEndpointDTO(
        id = mediaId,
        fileName = originalFileName,
        extension = extension,
        mimeType = mimeType,
        mediaType = mediaType,
        sizeBytes = sizeBytes,
        url = "/web/media/$mediaId/content",
        createdAt = createdAt
    )
}

fun Message.toEndpointDTO(
    member: ChatMember,
    sender: UserEndpointDTO?,
    currentUserId: Int,
    attachments: List<MediaFileEndpointDTO> = emptyList()
): MessageEndpointDTO? {
    val messageId = id ?: return null
    return MessageEndpointDTO(
        id = messageId,
        idChatMember = idChatMember,
        idChat = member.idChat,
        senderUserId = member.idUser,
        sender = sender,
        value = value,
        type = type?.string ?: "text",
        isMine = member.idUser == currentUserId,
        attachments = attachments,
        createdAt = createdAt,
        viewedAt = viewedAt,
        deletedAt = deletedAt
    )
}
