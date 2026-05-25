package Sockets.Events

import Sockets.DTO.SocketEventDTO
import Sockets.SocketEventContext
import kotlinx.serialization.json.encodeToJsonElement

class ConnectionReadySocketEvent(
    private val connectionId: String,
    private val chatIds: List<Int>
) : SocketEvent {
    override suspend fun dispatch(context: SocketEventContext) {
        context.sendToConnection(
            connectionId,
            SocketEventDTO(
                event = "connection:ready",
                data = context.json.encodeToJsonElement(mapOf("chatIds" to chatIds))
            )
        )
    }
}
