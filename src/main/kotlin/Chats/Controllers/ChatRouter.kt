package Chats.Controllers

import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.server.application.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
fun Application.ChatRouting() {
    routing {
        route("/chats",
            {
                tags = listOf("chats")
            })
        {
            get("/{id}", {
                operationId = "getChatById"
                summary = "Get message by ID"
                description = "Retrieves detailed information for a specific message"
            }) {
//            val users = UserRepository().findAll()
//            call.respond(users)
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
