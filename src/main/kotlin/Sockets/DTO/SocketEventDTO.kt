package Sockets.DTO

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SocketEventDTO(
    val event: String,
    val requestId: String? = null,
    val data: JsonElement? = null
)

@Serializable
data class SocketErrorDTO(
    val code: String,
    val message: String
)
