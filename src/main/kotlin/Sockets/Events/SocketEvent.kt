package Sockets.Events

import Sockets.SocketEventContext

interface SocketEvent {
    suspend fun dispatch(context: SocketEventContext)
}
