package Sockets.Events

import Sockets.DTO.SocketEventDTO
import Sockets.SocketEventContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class NotificationSocketEvent(
    private val userId: Int,
    private val type: String,
    private val kind: String,
    private val title: String,
    private val message: String
) : SocketEvent {
    override suspend fun dispatch(context: SocketEventContext) {
        context.sendToUser(
            userId,
            SocketEventDTO(
                event = "notification:new",
                data = buildJsonObject {
                    put("type", type)
                    put("kind", kind)
                    put("title", title)
                    put("message", message)
                }
            )
        )
    }
}
