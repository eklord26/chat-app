package Sockets

import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.sync.Mutex

data class SocketConnection(
    val id: String,
    val userId: Int,
    val session: DefaultWebSocketServerSession,
    val sendMutex: Mutex = Mutex()
)
