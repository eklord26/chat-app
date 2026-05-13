package Roles.Controllers

import Roles.DTO.Role
import Roles.DTO.RoleFilter
import Roles.Services.RoleService
import Tokens.Services.AuthGuard
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

fun Application.RoleRouting() {

    val service = RoleService()
    val authGuard = AuthGuard()

    routing {
        route("/roles", { tags = listOf("roles") }) {

            get({
                summary = "Get roles by filter"
                request {
                    queryParameter<String>("name") { description = "Partial name match" }
                    queryParameter<Boolean>("isDeleted") { description = "Filter by soft-deleted status" }
                }
                response {
                    HttpStatusCode.OK to { body<List<Role>>() }
                    HttpStatusCode.NotFound to { description = "Roles not found" }
                }
            }) {
                authGuard.requireUserId(call) ?: return@get
                val filter = RoleFilter(
                    name = call.request.queryParameters["name"],
                    isDeleted = call.request.queryParameters["isDeleted"]?.toBoolean()
                )

                val roles = service.findByFilter(filter)

                if (roles.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, roles)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Roles not found")
                }
            }

            get("/{id}", {
                summary = "Get role by ID"
                request { pathParameter<Int>("id") { description = "Role ID" } }
                response {
                    HttpStatusCode.OK to { body<Role>() }
                    HttpStatusCode.NotFound to { description = "Role not found" }
                }
            }) {
                authGuard.requireUserId(call) ?: return@get
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val role = service.findById(id)
                if (role != null) {
                    call.respond(HttpStatusCode.OK, role)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Role not found")
                }
            }

            post({
                summary = "Create new role"
                response { HttpStatusCode.Created to { description = "Role created" } }
            }) {
                authGuard.requireUserId(call) ?: return@post
                val role = call.receive<Role>()
                val newId = service.create(role)
                if (newId != null) {
                    call.respond(HttpStatusCode.Created, mapOf("id" to newId))
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            put("/{id}", {
                summary = "Update role"
                request { pathParameter<Int>("id") }
            }) {
                authGuard.requireUserId(call) ?: return@put
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@put
                }
                val role = call.receive<Role>()
                val updated = service.update(id, role)

                if (updated) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }

            delete("/{id}", {
                summary = "Delete role"
                request { pathParameter<Int>("id") }
            }) {
                authGuard.requireUserId(call) ?: return@delete
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@delete
                }

                val deleted = service.softDelete(id)
                if (deleted) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
