package Messages.Controllers

import Messages.DTO.Message
import Messages.DTO.MessageFilter
import Messages.Repositories.MessageRepository
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
fun Application.MessageRouting() {

    val repo = MessageRepository();

    routing {
        route("/messages",
            {
                tags = listOf("messages")
            })
        {
            get({
                tags = listOf("messages")
                summary = "Get messages by filter"
                description = "Retrieves a list of messages matching the filter criteria"

                request {
                    queryParameter<String>("idChatMember") { description = "Filter by owner messages" }
                    queryParameter<String>("value") { description = "Filter by partial value of messages" }
                    queryParameter<String>("createdAt") { description = "Filter by created date" }
                    queryParameter<String>("deletedAt") { description = "Filter by deleted date" }
                    queryParameter<String>("viewedAt") { description = "Filter by viewed date" }
                    queryParameter<Boolean>("deleted") { description = "Filter by deleted status" }
                    queryParameter<Boolean>("type") { description = "Filter by type messages" }
                }

                response {
                    HttpStatusCode.OK to {
                        description = "List of found messages"
                        body<List<Message>>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "Message not found"
                    }
                }
            }) {
                val filter = MessageFilter(
                    idChatMember = call.request.queryParameters["idChatMember"]?.toInt(),
                    value = call.request.queryParameters["value"],
                    createdAt = call.request.queryParameters["createdAt"],
                    deletedAt = call.request.queryParameters["deletedAt"],
                    viewedAt = call.request.queryParameters["viewedAt"],
                    deleted = call.request.queryParameters["deleted"]?.toBoolean()
                )

                val messages = repo.findByFilter(filter)

                if (messages.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, messages)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Message not found")
                }
            }

            // Метод получения по ID. Путь "/messages/{id}"
            get("/{id}", {
                tags = listOf("messages")
                summary = "Get message by ID"
                description = "Retrieves detailed information for a specific message"

                request {
                    pathParameter<Int>("id") { description = "Message ID" }
                }

                response {
                    HttpStatusCode.OK to {
                        description = "Message found"
                        body<Message>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "Message not found"
                    }
                }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val message = repo.findById(id)
                if (message != null) {
                    call.respond(HttpStatusCode.OK, message)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Message not found")
                }
            }
            post {
//            val id = call.request.queryParameters["id"]
//
//            if (id !== null) {
//                val user = UserRepository().findById(id.toInt())
//
//                if (user !== null) {
//                    call.respond(user)
//                }
//            }
            }
            put {

            }
            patch {

            }
            delete("/{id}", {
                operationId = "deleteMessageById"
                summary = "Delete message by ID"
                description = "Deletes message by ID"
            }) {

            }
        }
    }
}
