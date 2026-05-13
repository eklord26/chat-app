package Messages.Controllers

import Messages.DTO.Message
import Messages.DTO.MessageFilter
import Messages.Services.MessageService
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

fun Application.MessageRouting() {
    val service = MessageService()
    val authGuard = AuthGuard()

    routing {
        route("/messages", {
            tags = listOf("messages")
        }) {
            get({
                summary = "Get messages by filter"
                request {
                    queryParameter<Int>("idChatMember") { description = "Filter by sender member ID" }
                    queryParameter<String>("value") { description = "Search in message text" }
                    queryParameter<String>("type") { description = "Filter by message type (TEXT, PHOTO, etc.)" }
                    queryParameter<Boolean>("isDeleted") { description = "Filter by soft-deleted status" }
                }
                response {
                    HttpStatusCode.OK to { body<List<Message>>() }
                    HttpStatusCode.NotFound to { description = "Messages not found" }
                }
            }) {
                authGuard.requireUserId(call) ?: return@get
                val filter = MessageFilter(
                    idChatMember = call.request.queryParameters["idChatMember"]?.toIntOrNull(),
                    value = call.request.queryParameters["value"],
                    isDeleted = call.request.queryParameters["isDeleted"]?.toBoolean()
                )

                val messages = service.findByFilter(filter)
                if (messages.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, messages)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Message not found")
                }
            }

            get("/{id}", {
                summary = "Get message by ID"
                request { pathParameter<Int>("id") }
                response {
                    HttpStatusCode.OK to { body<Message>() }
                    HttpStatusCode.NotFound to { description = "Message not found" }
                }
            }) {
                authGuard.requireUserId(call) ?: return@get
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val message = service.findById(id)
                if (message != null) {
                    call.respond(HttpStatusCode.OK, message)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Message not found")
                }
            }

            post({
                summary = "Send new message"
                response { HttpStatusCode.Created to { description = "Message sent" } }
            }) {
                authGuard.requireUserId(call) ?: return@post
                val message = call.receive<Message>()
                val newId = service.create(message)
                if (newId != null) {
                    call.respond(HttpStatusCode.Created, mapOf("id" to newId))
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            put("/{id}", {
                summary = "Update message content"
                request { pathParameter<Int>("id") }
            }) {
                authGuard.requireUserId(call) ?: return@put
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@put
                }
                val message = call.receive<Message>()
                if (service.update(id, message)) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            delete("/{id}", {
                summary = "Soft delete message"
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
