package Sockets

import io.ktor.websocket.Frame
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object SocketConnectionRegistry {
    private val connections = ConcurrentHashMap<String, SocketConnection>()
    private val userConnections = ConcurrentHashMap<Int, MutableSet<String>>()
    private val chatConnections = ConcurrentHashMap<Int, MutableSet<String>>()

    fun register(userId: Int, session: io.ktor.server.websocket.DefaultWebSocketServerSession): SocketConnection {
        val connection = SocketConnection(
            id = UUID.randomUUID().toString(),
            userId = userId,
            session = session
        )

        connections[connection.id] = connection
        userConnections.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }.add(connection.id)
        return connection
    }

    fun joinChats(connectionId: String, chatIds: Collection<Int>) {
        chatIds.forEach { chatId ->
            chatConnections.computeIfAbsent(chatId) { ConcurrentHashMap.newKeySet() }.add(connectionId)
        }
    }

    fun joinUserToChat(userId: Int, chatId: Int) {
        userConnections[userId].orEmpty().forEach { connectionId ->
            chatConnections.computeIfAbsent(chatId) { ConcurrentHashMap.newKeySet() }.add(connectionId)
        }
    }

    fun unregister(connectionId: String) {
        val connection = connections.remove(connectionId) ?: return
        userConnections[connection.userId]?.remove(connectionId)
        chatConnections.values.forEach { it.remove(connectionId) }
    }

    suspend fun sendToConnection(connectionId: String, text: String) {
        val connection = connections[connectionId] ?: return
        runCatching {
            connection.sendMutex.withLock {
                connection.session.send(Frame.Text(text))
            }
        }.onFailure {
            unregister(connectionId)
        }
    }

    suspend fun broadcastToChat(chatId: Int, text: String) {
        chatConnections[chatId].orEmpty().toList().forEach { connectionId ->
            sendToConnection(connectionId, text)
        }
    }

    suspend fun sendToUser(userId: Int, text: String) {
        userConnections[userId].orEmpty().toList().forEach { connectionId ->
            sendToConnection(connectionId, text)
        }
    }
}
