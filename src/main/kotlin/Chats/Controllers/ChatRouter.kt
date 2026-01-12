package Chats.Controllers

import Chats.DTO.Chat
import Chats.DTO.ChatFilter
import Chats.Repositories.ChatRepository
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
fun Application.ChatRouting() {
    val repo = ChatRepository()
    routing {
        route("/chats",
            {
                tags = listOf("chats")
            })
        {
            get({
                tags = listOf("chats")
                summary = "Get chats by filter"
                description = "Retrieves a list of chats matching the filter criteria"

                request {
                    queryParameter<String>("owner") { description = "Filter by owner chats" }
                    queryParameter<String>("name") { description = "Filter by partial name of chats" }
                    queryParameter<String>("createdAt") { description = "Filter by created date" }
                    queryParameter<Boolean>("deleted") { description = "Filter by deleted status" }
                }

                response {
                    HttpStatusCode.OK to {
                        description = "List of found chats"
                        body<List<Chat>>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "Chat not found"
                    }
                }
            }) {
                val filter = ChatFilter(
                    owner = call.request.queryParameters["owner"]?.toInt(),
                    name = call.request.queryParameters["name"],
                    createdAt = call.request.queryParameters["createdAt"],
                    deleted = call.request.queryParameters["deleted"]?.toBoolean()
                )

                val chats = repo.findByFilter(filter)

                if (chats.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, chats)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Chat not found")
                }
            }

            // Метод получения по ID. Путь "/chats/{id}"
            get("/{id}", {
                tags = listOf("chats")
                summary = "Get chat by ID"
                description = "Retrieves detailed information for a specific chat"

                request {
                    pathParameter<Int>("id") { description = "Chat ID" }
                }

                response {
                    HttpStatusCode.OK to {
                        description = "Chat found"
                        body<Chat>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "Chat not found"
                    }
                }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val chat = repo.findById(id)
                if (chat != null) {
                    call.respond(HttpStatusCode.OK, chat)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Chat not found")
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
                operationId = "deleteChatById"
                summary = "Delete chat by ID"
                description = "Deletes chat by ID"
            }) {

            }
        }
    }
}
