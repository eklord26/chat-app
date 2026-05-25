package com.example

import Chats.Services.ChatAccessService
import Sockets.DTO.SocketEventDTO
import Sockets.SocketBroadcaster
import Sockets.SocketConnectionRegistry
import Tokens.Services.TokenService
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets() {
    val tokenService = TokenService()
    val chatAccessService = ChatAccessService(environment)
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(json)
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/ws") {
            val token = call.request.queryParameters["token"]
                ?: call.request.headers["Authorization"]?.substringAfter("Bearer ", missingDelimiterValue = "")
                    ?.takeIf { it.isNotBlank() }
                ?: call.request.headers["Auth-Token"]?.takeIf { it.isNotBlank() }

            val userId = token?.let { tokenService.getUserIdByToken(it) }
            if (userId == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))
                return@webSocket
            }

            val connection = SocketConnectionRegistry.register(userId, this)
            val chatIds = chatAccessService.userActiveChatIds(userId)
            SocketConnectionRegistry.joinChats(connection.id, chatIds)
            SocketBroadcaster.connectionReady(connection.id, chatIds)

            try {
                for (frame in incoming) {
                    if (frame !is Frame.Text) continue

                    val text = frame.readText()
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        break
                    }

                    val event = runCatching {
                        json.decodeFromString<SocketEventDTO>(text)
                    }.getOrNull()

                    when (event?.event ?: text) {
                        "ping" -> SocketConnectionRegistry.sendToConnection(
                            connection.id,
                            json.encodeToString(
                                SocketEventDTO(
                                    event = "pong",
                                    requestId = event?.requestId
                                )
                            )
                        )

                        else -> SocketBroadcaster.error(
                            connection.id,
                            "UNSUPPORTED_SOCKET_EVENT",
                            "This WebSocket endpoint accepts service events only. Send messages through REST API.",
                            event?.requestId
                        )
                    }
                }
            } finally {
                SocketConnectionRegistry.unregister(connection.id)
            }
        }
    }
}
