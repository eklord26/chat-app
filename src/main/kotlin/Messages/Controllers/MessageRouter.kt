package Messages.Controllers

import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.server.application.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
fun Application.MessageRouting() {
    routing {
        route("/messages",
            {
                tags = listOf("messages")
            })
        {
            get("/{id}", {
                operationId = "getMessageById"
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
                operationId = "deleteMessageById"
                summary = "Delete message by ID"
                description = "Deletes message by ID"
            }) {

            }
        }
    }
}
