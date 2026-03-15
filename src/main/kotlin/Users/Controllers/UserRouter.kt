package Users.Controllers

import Users.DTO.UserFilter
import com.example.Users.DTO.User
import com.example.Users.Services.UserService
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.UserRouting() {

    val service = UserService()

    routing {
        route("/users", { tags = listOf("users") }) {

            get({
                summary = "Get users by filter"
                request {
                    queryParameter<String>("login") { description = "Filter by exact login" }
                    queryParameter<String>("name") { description = "Filter by partial name" }
                    queryParameter<Boolean>("isAdmin") { description = "Filter by admin status" }
                    queryParameter<Boolean>("isDeleted") { description = "Filter by deleted status" }
                }
                response {
                    HttpStatusCode.OK to { body<List<User>>() }
                    HttpStatusCode.NotFound to { description = "User not found" }
                }
            }) {
                val filter = UserFilter(
                    login = call.request.queryParameters["login"],
                    name = call.request.queryParameters["name"],
                    isAdmin = call.request.queryParameters["isAdmin"]?.toBoolean(),
                    isDeleted = call.request.queryParameters["isDeleted"]?.toBoolean()
                )

                val users = service.findByFilter(filter)

                if (users.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, users)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }

            get("/{id}", {
                summary = "Get user by ID"
                request { pathParameter<Int>("id") { description = "User ID" } }
                response {
                    HttpStatusCode.OK to { body<User>() }
                    HttpStatusCode.NotFound to { description = "User not found" }
                }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val user = service.findById(id)
                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }

            post({
                summary = "Create user"
                response { HttpStatusCode.Created to { description = "User created" } }
            }) {
                val user = call.receive<User>()
                val newId = service.create(user)
                if (newId != null) {
                    call.respond(HttpStatusCode.Created, mapOf("id" to newId))
                } else {
                    call.respond(HttpStatusCode.Conflict, "Login already exists")
                }
            }

            put("/{id}", {
                summary = "Update user"
                request { pathParameter<Int>("id") }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@put
                }
                val user = call.receive<User>()
                val updated = service.update(id, user)
                if (updated) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }

            delete("/{id}", {
                summary = "Delete user by ID"
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@delete
                }
                val deleted = service.delete(id)
                if (deleted) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}