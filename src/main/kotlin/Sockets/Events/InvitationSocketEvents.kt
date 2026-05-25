package Sockets.Events

import Sockets.DTO.SocketEventDTO
import Sockets.SocketEventContext
import Web.DTO.ChatInvitationEndpointDTO
import Web.DTO.ContactInvitationEndpointDTO
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

class ContactInvitationCreatedSocketEvent(
    private val senderUserId: Int,
    private val receiverUserId: Int,
    private val invitation: ContactInvitationEndpointDTO
) : SocketEvent {
    override suspend fun dispatch(context: SocketEventContext) {
        dispatchInvitation(
            context = context,
            eventName = "invitation:created",
            kind = "contact",
            outgoingUserId = senderUserId,
            targetUserIds = listOf(senderUserId, receiverUserId),
            invitation = context.json.encodeToJsonElement(invitation)
        )
    }
}

class ChatInvitationCreatedSocketEvent(
    private val senderUserId: Int,
    private val receiverUserId: Int,
    private val invitation: ChatInvitationEndpointDTO
) : SocketEvent {
    override suspend fun dispatch(context: SocketEventContext) {
        dispatchInvitation(
            context = context,
            eventName = "invitation:created",
            kind = "chat",
            outgoingUserId = senderUserId,
            targetUserIds = listOf(senderUserId, receiverUserId),
            invitation = context.json.encodeToJsonElement(invitation)
        )
    }
}

class ContactInvitationUpdatedSocketEvent(
    private val senderUserId: Int,
    private val receiverUserId: Int,
    private val invitation: ContactInvitationEndpointDTO
) : SocketEvent {
    override suspend fun dispatch(context: SocketEventContext) {
        dispatchInvitation(
            context = context,
            eventName = "invitation:updated",
            kind = "contact",
            outgoingUserId = senderUserId,
            targetUserIds = listOf(senderUserId, receiverUserId),
            invitation = context.json.encodeToJsonElement(invitation)
        )
    }
}

class ChatInvitationUpdatedSocketEvent(
    private val senderUserId: Int,
    private val receiverUserId: Int,
    private val invitation: ChatInvitationEndpointDTO
) : SocketEvent {
    override suspend fun dispatch(context: SocketEventContext) {
        if (invitation.status == "accepted") {
            context.joinUserToChat(receiverUserId, invitation.idChat)
        }

        dispatchInvitation(
            context = context,
            eventName = "invitation:updated",
            kind = "chat",
            outgoingUserId = senderUserId,
            targetUserIds = listOf(senderUserId, receiverUserId),
            invitation = context.json.encodeToJsonElement(invitation)
        )
    }
}

private suspend fun dispatchInvitation(
    context: SocketEventContext,
    eventName: String,
    kind: String,
    outgoingUserId: Int,
    targetUserIds: List<Int>,
    invitation: JsonElement
) {
    targetUserIds.distinct().forEach { userId ->
        val direction = if (userId == outgoingUserId) "outgoing" else "incoming"
        context.sendToUser(
            userId,
            SocketEventDTO(
                event = eventName,
                data = buildJsonObject {
                    put("kind", kind)
                    put("direction", direction)
                    put("invitation", invitation)
                }
            )
        )
    }
}
