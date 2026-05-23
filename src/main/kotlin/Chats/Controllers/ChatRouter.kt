package Chats.Controllers

import Chats.DTO.Chat
import Chats.DTO.ChatFilter
import Chats.Services.ChatService
import Tokens.Services.AuthGuard
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.ChatRouting() {
    val service = ChatService(environment)
    val authGuard = AuthGuard()

    routing {
        route("/chats", {
            tags = listOf("chats")
        }) {
            get({
                summary = "Get chats by filter"
                request {
                    queryParameter<Int>("owner") { description = "Filter by owner ID" }
                    queryParameter<String>("name") { description = "Filter by partial name" }
                    queryParameter<String>("createdAt") { description = "Filter by exact creation date (ISO Instant)" }
                    queryParameter<Boolean>("isDeleted") { description = "Filter by deleted status" }
                }
                response {
                    HttpStatusCode.OK to { body<List<Chat>>() }
                    HttpStatusCode.NotFound to { description = "Chats not found" }
                }
            }) {
                authGuard.requireUserId(call) ?: return@get
                val filter = ChatFilter(
                    owner = call.request.queryParameters["owner"]?.toIntOrNull(),
                    name = call.request.queryParameters["name"],
                    createdAt = call.request.queryParameters["createdAt"],
                    isDeleted = call.request.queryParameters["isDeleted"]?.toBoolean()
                )

                val chats = service.findByFilter(filter)
                if (chats.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, chats)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Chat not found")
                }
            }

            get("/{id}", {
                summary = "Get chat by ID"
                request { pathParameter<Int>("id") }
                response {
                    HttpStatusCode.OK to { body<Chat>() }
                    HttpStatusCode.NotFound to { description = "Chat not found" }
                }
            }) {
                authGuard.requireUserId(call) ?: return@get
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val chat = service.findById(id)
                if (chat != null) {
                    call.respond(HttpStatusCode.OK, chat)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Chat not found")
                }
            }

            post({
                summary = "Create new chat"
                response { HttpStatusCode.Created to { description = "Chat created" } }
            }) {
                val idUser = authGuard.requireUserId(call) ?: return@post
                val chat = call.receive<Chat>().copy(owner = idUser)
                val newId = service.create(chat)
                if (newId != null) {
                    call.respond(HttpStatusCode.Created, mapOf("id" to newId))
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            put("/{id}", {
                summary = "Update chat"
                request { pathParameter<Int>("id") }
            }) {
                authGuard.requireUserId(call) ?: return@put
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@put
                }
                val chat = call.receive<Chat>()
                if (service.update(id, chat)) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            delete("/{id}", {
                summary = "Soft delete chat"
                request { pathParameter<Int>("id") }
            }) {
                authGuard.requireUserId(call) ?: return@delete
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null && service.softDelete(id)) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
