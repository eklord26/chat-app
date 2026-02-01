package Rights.Controllers

import Rights.DTO.Right
import Rights.DTO.RightFilter
import Rights.Repositories.RightRepository
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
fun Application.RightRouting() {

    val repo = RightRepository()

    routing {
        route("/rights",
            {
                tags = listOf("rights")
            })
        {
            get({
                tags = listOf("rights")
                summary = "Get rights by filter"
                description = "Retrieves a list of rights matching the filter criteria"

                request {
                    queryParameter<String>("idRole") { description = "Filter by id role" }
                    queryParameter<String>("name") { description = "Filter by name" }
                    queryParameter<Boolean>("deletedAt") { description = "Filter deleted at" }
                }

                response {
                    HttpStatusCode.OK to {
                        description = "List of found rights"
                        body<List<Right>>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "right not found"
                    }
                }
            }) {
                val filter = RightFilter(
                    idRole = call.request.queryParameters["idRole"]?.toInt(),
                    name = call.request.queryParameters["name"],
                    deletedAt = call.request.queryParameters["deletedAt"]
                )

                val rights = repo.findByFilter(filter)

                if (rights.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, rights)
                } else {
                    call.respond(HttpStatusCode.NotFound, "right not found")
                }
            }

            // Метод получения по ID. Путь "/rights/{id}"
            get("/{id}", {
                tags = listOf("rights")
                summary = "Get right by ID"
                description = "Retrieves detailed information for a specific right"

                request {
                    pathParameter<Int>("id") { description = "right ID" }
                }

                response {
                    HttpStatusCode.OK to {
                        description = "right found"
                        body<Right>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "right not found"
                    }
                }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val right = repo.findById(id)
                if (right != null) {
                    call.respond(HttpStatusCode.OK, right)
                } else {
                    call.respond(HttpStatusCode.NotFound, "right not found")
                }
            }
            post {
                val id = call.request.queryParameters["id"]

                if ((id !== null) && id.toIntOrNull() != null) {
                    val right = RightRepository().findById(id.toInt())

                    if (right !== null) {
                        call.respond(right)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "right not found")
                    }
                }
            }
            put {

            }
            patch {

            }
            delete("/{id}", {
                operationId = "deleteRightById"
                summary = "Delete right by ID"
                description = "Delete right by ID"
            }) {

            }
        }
    }
}
