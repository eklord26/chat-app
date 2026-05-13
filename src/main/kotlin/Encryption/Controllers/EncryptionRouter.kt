package Encryption.Controllers

import Encryption.DTO.ChatEncryptionKeyResponse
import Encryption.DTO.ChatEncryptionKeysResponse
import Encryption.Services.ChatEncryptionKeyService
import Tokens.Services.AuthGuard
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.routing

fun Application.EncryptionRouting() {
    val service = ChatEncryptionKeyService(environment)
    val authGuard = AuthGuard()

    routing {
        route("/encryption", {
            tags = listOf("encryption")
        }) {
            get("/chats/{idChat}/key", {
                summary = "Get or create active chat encryption key"
                request {
                    pathParameter<Int>("idChat")
                }
                response {
                    HttpStatusCode.OK to { body<ChatEncryptionKeyResponse>() }
                    HttpStatusCode.Unauthorized to { description = "Missing or invalid auth token" }
                    HttpStatusCode.Forbidden to { description = "User is not an active chat member" }
                }
            }) {
                val idChat = call.parameters["idChat"]?.toIntOrNull()
                if (idChat == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid chat ID")
                    return@get
                }

                val idUser = authGuard.requireUserId(call)
                if (idUser == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val key = service.getOrCreateForUser(idChat, idUser)
                if (key == null) call.respond(HttpStatusCode.Forbidden)
                else call.respond(HttpStatusCode.OK, key)
            }

            get("/chats/{idChat}/keys", {
                summary = "Get all chat encryption keys available to user"
                request {
                    pathParameter<Int>("idChat")
                }
                response {
                    HttpStatusCode.OK to { body<ChatEncryptionKeysResponse>() }
                    HttpStatusCode.Unauthorized to { description = "Missing or invalid auth token" }
                    HttpStatusCode.Forbidden to { description = "User has no access to chat keys" }
                }
            }) {
                val idChat = call.parameters["idChat"]?.toIntOrNull()
                if (idChat == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid chat ID")
                    return@get
                }

                val idUser = authGuard.requireUserId(call)
                if (idUser == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val keys = service.findAccessibleKeysForUser(idChat, idUser)
                if (keys == null) call.respond(HttpStatusCode.Forbidden)
                else call.respond(HttpStatusCode.OK, keys)
            }

            get("/chats/{idChat}/keys/{version}", {
                summary = "Get chat encryption key by version"
                request {
                    pathParameter<Int>("idChat")
                    pathParameter<Int>("version")
                }
                response {
                    HttpStatusCode.OK to { body<ChatEncryptionKeyResponse>() }
                    HttpStatusCode.Unauthorized to { description = "Missing or invalid auth token" }
                    HttpStatusCode.Forbidden to { description = "User has no access to requested key" }
                }
            }) {
                val idChat = call.parameters["idChat"]?.toIntOrNull()
                val version = call.parameters["version"]?.toIntOrNull()
                if (idChat == null || version == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid chat or key version")
                    return@get
                }

                val idUser = authGuard.requireUserId(call)
                if (idUser == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val key = service.findByVersionForUser(idChat, idUser, version)
                if (key == null) call.respond(HttpStatusCode.Forbidden)
                else call.respond(HttpStatusCode.OK, key)
            }

            post("/chats/{idChat}/key/rotate", {
                summary = "Rotate active chat encryption key"
                request { pathParameter<Int>("idChat") }
                response {
                    HttpStatusCode.OK to { body<ChatEncryptionKeyResponse>() }
                    HttpStatusCode.Unauthorized to { description = "Missing or invalid auth token" }
                    HttpStatusCode.Forbidden to { description = "User is not an active chat member" }
                }
            }) {
                val idChat = call.parameters["idChat"]?.toIntOrNull()
                if (idChat == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid chat ID")
                    return@post
                }

                val idUser = authGuard.requireUserId(call)
                if (idUser == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val key = service.rotateForUser(idChat, idUser)
                if (key == null) call.respond(HttpStatusCode.Forbidden)
                else call.respond(HttpStatusCode.OK, key)
            }
        }
    }
}
