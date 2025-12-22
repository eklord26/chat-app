package ChatMembers.Controllers

import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.server.application.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
fun Application.ChatMemberRouting() {
    routing {
        route("/members",
            {
                tags = listOf("members")
            })
        {
            get("/{id}", {
                operationId = "getChatMemberById"
                summary = "Get user by ID"
                description = "Retrieves detailed information for a specific user"
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
                operationId = "deleteUserById"
                summary = "Delete user by ID"
                description = "Deletes user by ID"
            }) {

            }
        }
    }
}
