package Users.Controllers

import Users.DTO.UserFilter
import com.example.Users.DTO.User
import com.example.Users.Repository.UserRepository
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
fun Application.UserRouting() {

    val repo = UserRepository()

    routing {
        route("/users",
            {
                tags = listOf("users")
            })
        {
            get({
                tags = listOf("users")
                summary = "Get users by filter"
                description = "Retrieves a list of users matching the filter criteria"

                request {
                    queryParameter<String>("login") { description = "Filter by exact login" }
                    queryParameter<String>("name") { description = "Filter by partial name" }
                    queryParameter<Boolean>("isAdmin") { description = "Filter by admin status" }
                    queryParameter<Boolean>("deleted") { description = "Filter by deleted status" }
                }

                response {
                    HttpStatusCode.OK to {
                        description = "List of found users"
                        body<List<User>>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "User not found"
                    }
                }
            }) {
                val filter = UserFilter(
                    login = call.request.queryParameters["login"],
                    name = call.request.queryParameters["name"],
                    isAdmin = call.request.queryParameters["isAdmin"]?.toBoolean(),
                    deleted = call.request.queryParameters["deleted"]?.toBoolean()
                )

                val users = repo.findByFilter(filter)

                if (users.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, users)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }

            // Метод получения по ID. Путь "/users/{id}"
            get("/{id}", {
                tags = listOf("users")
                summary = "Get user by ID"
                description = "Retrieves detailed information for a specific user"

                request {
                    pathParameter<Int>("id") { description = "User ID" }
                }

                response {
                    HttpStatusCode.OK to {
                        description = "User found"
                        body<User>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "User not found"
                    }
                }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val user = repo.findById(id)
                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }
            post {
            val id = call.request.queryParameters["id"]

            if ((id !== null) && id.toIntOrNull() != null) {
                val user = UserRepository().findById(id.toInt())

                if (user !== null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }
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
