package Sockets.Events

import Sockets.DTO.SocketErrorDTO
import Sockets.DTO.SocketEventDTO
import Sockets.SocketEventContext
import kotlinx.serialization.json.encodeToJsonElement

class ErrorSocketEvent(
    private val connectionId: String,
    private val code: String,
    private val message: String,
    private val requestId: String? = null
) : SocketEvent {
    override suspend fun dispatch(context: SocketEventContext) {
        context.sendToConnection(
            connectionId,
            SocketEventDTO(
                event = "error",
                requestId = requestId,
                data = context.json.encodeToJsonElement(SocketErrorDTO(code, message))
            )
        )
    }
}
