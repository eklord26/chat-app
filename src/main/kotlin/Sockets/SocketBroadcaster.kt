package Sockets

import Messages.DTO.CreatedMessageResult
import Sockets.Events.ChatInvitationCreatedSocketEvent
import Sockets.Events.ChatInvitationUpdatedSocketEvent
import Sockets.Events.ConnectionReadySocketEvent
import Sockets.Events.ContactInvitationCreatedSocketEvent
import Sockets.Events.ContactInvitationUpdatedSocketEvent
import Sockets.Events.ErrorSocketEvent
import Sockets.Events.MessageCreatedSocketEvent
import Sockets.Events.NotificationSocketEvent
import Sockets.Events.SocketEvent
import Web.DTO.ChatInvitationEndpointDTO
import Web.DTO.ContactInvitationEndpointDTO

object SocketBroadcaster {
    private val dispatcher = SocketEventDispatcher()

    suspend fun publish(events: List<SocketEvent>) {
        dispatcher.publish(events)
    }

    suspend fun connectionReady(connectionId: String, chatIds: List<Int>) {
        dispatcher.publish(ConnectionReadySocketEvent(connectionId, chatIds))
    }

    suspend fun messageCreated(result: CreatedMessageResult) {
        dispatcher.publish(MessageCreatedSocketEvent(result))
    }

    suspend fun contactInvitationCreated(senderUserId: Int, receiverUserId: Int, invitation: ContactInvitationEndpointDTO) {
        publish(
            listOf(
                ContactInvitationCreatedSocketEvent(senderUserId, receiverUserId, invitation),
                NotificationSocketEvent(
                    userId = receiverUserId,
                    type = "invitation",
                    kind = "contact",
                    title = "Новое приглашение в контакты",
                    message = invitation.sender?.name?.let { "$it приглашает вас в контакты" }
                        ?: "Вам отправили приглашение в контакты"
                )
            )
        )
    }

    suspend fun chatInvitationCreated(senderUserId: Int, receiverUserId: Int, invitation: ChatInvitationEndpointDTO) {
        publish(
            listOf(
                ChatInvitationCreatedSocketEvent(senderUserId, receiverUserId, invitation),
                NotificationSocketEvent(
                    userId = receiverUserId,
                    type = "invitation",
                    kind = "chat",
                    title = "Новое приглашение в чат",
                    message = invitation.chat?.name?.let { "Вас пригласили в чат \"$it\"" }
                        ?: "Вас пригласили в чат"
                )
            )
        )
    }

    suspend fun contactInvitationUpdated(
        actorUserId: Int,
        senderUserId: Int,
        receiverUserId: Int,
        invitation: ContactInvitationEndpointDTO
    ) {
        publish(
            listOfNotNull(
                ContactInvitationUpdatedSocketEvent(senderUserId, receiverUserId, invitation),
                notificationForOtherParticipant(
                    actorUserId = actorUserId,
                    senderUserId = senderUserId,
                    receiverUserId = receiverUserId,
                    kind = "contact",
                    title = "Статус приглашения обновлен",
                    message = "Приглашение в контакты: ${invitation.status}"
                )
            )
        )
    }

    suspend fun chatInvitationUpdated(
        actorUserId: Int,
        senderUserId: Int,
        receiverUserId: Int,
        invitation: ChatInvitationEndpointDTO
    ) {
        publish(
            listOfNotNull(
                ChatInvitationUpdatedSocketEvent(senderUserId, receiverUserId, invitation),
                notificationForOtherParticipant(
                    actorUserId = actorUserId,
                    senderUserId = senderUserId,
                    receiverUserId = receiverUserId,
                    kind = "chat",
                    title = "Статус приглашения в чат обновлен",
                    message = invitation.chat?.name?.let { "$it: ${invitation.status}" }
                        ?: "Приглашение в чат: ${invitation.status}"
                )
            )
        )
    }

    suspend fun error(connectionId: String, code: String, message: String, requestId: String? = null) {
        dispatcher.publish(ErrorSocketEvent(connectionId, code, message, requestId))
    }

    private fun notificationForOtherParticipant(
        actorUserId: Int,
        senderUserId: Int,
        receiverUserId: Int,
        kind: String,
        title: String,
        message: String
    ): NotificationSocketEvent? {
        val targetUserId = listOf(senderUserId, receiverUserId)
            .firstOrNull { it != actorUserId }
            ?: return null

        return NotificationSocketEvent(
            userId = targetUserId,
            type = "invitation",
            kind = kind,
            title = title,
            message = message
        )
    }
}
