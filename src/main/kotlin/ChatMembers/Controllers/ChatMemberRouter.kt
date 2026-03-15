package ChatMembers.Controllers

import ChatMembers.DTO.ChatMember
import ChatMembers.DTO.ChatMemberFilter
import ChatMembers.Services.ChatMemberService
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

fun Application.ChatMemberRouting() {
    val service = ChatMemberService()

    routing {
        route("/members", {
            tags = listOf("members")
        }) {
            get({
                summary = "Get members by filter"
                request {
                    queryParameter<Int>("idChat") { description = "Filter by Chat ID" }
                    queryParameter<Int>("idUser") { description = "Filter by User ID" }
                    queryParameter<Int>("idRole") { description = "Filter by Role ID" }
                    queryParameter<Boolean>("isDeleted") { description = "Filter by deleted status" }
                }
                response {
                    HttpStatusCode.OK to { body<List<ChatMember>>() }
                    HttpStatusCode.NotFound to { description = "Members not found" }
                }
            }) {
                val filter = ChatMemberFilter(
                    idChat = call.request.queryParameters["idChat"]?.toIntOrNull(),
                    idUser = call.request.queryParameters["idUser"]?.toIntOrNull(),
                    idRole = call.request.queryParameters["idRole"]?.toIntOrNull(),
                    isDeleted = call.request.queryParameters["isDeleted"]?.toBoolean()
                )

                val members = service.findByFilter(filter)
                if (members.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, members)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Members not found")
                }
            }

            get("/{id}", {
                summary = "Get member by ID"
                request { pathParameter<Int>("id") }
                response {
                    HttpStatusCode.OK to { body<ChatMember>() }
                    HttpStatusCode.NotFound to { description = "Member not found" }
                }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val member = service.findById(id)
                if (member != null) {
                    call.respond(HttpStatusCode.OK, member)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Member not found")
                }
            }

            post({
                summary = "Add member to chat"
                response { HttpStatusCode.Created to { description = "Member added" } }
            }) {
                val member = call.receive<ChatMember>()
                val newId = service.create(member)
                if (newId != null) {
                    call.respond(HttpStatusCode.Created, mapOf("id" to newId))
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            put("/{id}", {
                summary = "Update member"
                request { pathParameter<Int>("id") }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@put
                }
                val member = call.receive<ChatMember>()
                if (service.update(id, member)) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            delete("/{id}", {
                summary = "Soft delete member"
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