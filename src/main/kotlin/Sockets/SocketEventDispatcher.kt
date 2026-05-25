package Sockets

import Sockets.Events.SocketEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SocketEventDispatcher(
    private val registry: SocketConnectionRegistry = SocketConnectionRegistry,
    private val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
) {
    suspend fun publish(event: SocketEvent) {
        event.dispatch(SocketEventContext(registry, json))
    }

    suspend fun publish(events: List<SocketEvent>) {
        events.forEach { publish(it) }
    }
}

class SocketEventContext(
    private val registry: SocketConnectionRegistry,
    val json: Json
) {
    suspend fun sendToConnection(connectionId: String, event: Sockets.DTO.SocketEventDTO) {
        registry.sendToConnection(connectionId, json.encodeToString(event))
    }

    suspend fun sendToUser(userId: Int, event: Sockets.DTO.SocketEventDTO) {
        registry.sendToUser(userId, json.encodeToString(event))
    }

    suspend fun broadcastToChat(chatId: Int, event: Sockets.DTO.SocketEventDTO) {
        registry.broadcastToChat(chatId, json.encodeToString(event))
    }

    fun joinUserToChat(userId: Int, chatId: Int) {
        registry.joinUserToChat(userId, chatId)
    }
}
