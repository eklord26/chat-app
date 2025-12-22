package Rights.Controllers

import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.server.application.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
fun Application.RightRouting() {
    routing {
        route("/rights",
            {
                tags = listOf("rights")
            })
        {
            get("/{id}", {
                operationId = "getRightById"
                summary = "Get right by ID"
                description = "Retrieves detailed information for a specific right"
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
                operationId = "deleteRightById"
                summary = "Delete right by ID"
                description = "Deletes right by ID"
            }) {

            }
        }
    }
}
