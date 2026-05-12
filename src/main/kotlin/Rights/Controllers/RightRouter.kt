package Rights.Controllers

import Rights.DTO.Right
import Rights.DTO.RightFilter
import Rights.Services.RightService
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

fun Application.RightRouting() {
    val service = RightService()

    routing {
        route("/rights", {
            tags = listOf("rights")
        }) {
            get({
                summary = "Get rights by filter"
                request {
                    queryParameter<Int>("idRole") { description = "Filter by Role ID" }
                    queryParameter<String>("name") { description = "Filter by partial name" }
                    queryParameter<Boolean>("isDeleted") { description = "Filter by soft-deleted status" }
                }
                response {
                    HttpStatusCode.OK to { body<List<Right>>() }
                    HttpStatusCode.NotFound to { description = "Rights not found" }
                }
            }) {
                val filter = RightFilter(
                    idRole = call.request.queryParameters["idRole"]?.toIntOrNull(),
                    name = call.request.queryParameters["name"],
                    isDeleted = call.request.queryParameters["isDeleted"]?.toBoolean()
                )

                val rights = service.findByFilter(filter)
                if (rights.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, rights)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Rights not found")
                }
            }

            get("/{id}", {
                summary = "Get right by ID"
                request { pathParameter<Int>("id") }
                response {
                    HttpStatusCode.OK to { body<Right>() }
                    HttpStatusCode.NotFound to { description = "Right not found" }
                }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val right = service.findById(id)
                if (right != null) {
                    call.respond(HttpStatusCode.OK, right)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Right not found")
                }
            }

            post({
                summary = "Create new right"
                response { HttpStatusCode.Created to { description = "Right created" } }
            }) {
                val right = call.receive<Right>()
                val newId = service.create(right)
                if (newId != null) {
                    call.respond(HttpStatusCode.Created, mapOf("id" to newId))
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            put("/{id}", {
                summary = "Update right"
                request { pathParameter<Int>("id") }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@put
                }
                val right = call.receive<Right>()
                if (service.update(id, right)) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            delete("/{id}", {
                summary = "Soft delete right"
                request { pathParameter<Int>("id") }
            }) {
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