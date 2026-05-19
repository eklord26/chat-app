package Invitations.Controllers

import Invitations.DTO.ContactInvitation
import Invitations.DTO.ContactInvitationFilter
import Invitations.DTO.CreateContactInvitationBodyDTO
import Invitations.Enums.InvitationStatusEnum
import Invitations.Services.ContactInvitationService
import Tokens.Services.AuthGuard
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import java.time.Instant

fun Application.ContactInvitationRouting() {
    val service = ContactInvitationService()
    val authGuard = AuthGuard()

    routing {
        route("/contact-invitations", { tags = listOf("contact_invitations") }) {
            get({
                summary = "Get contact invitations by filter"
                request {
                    queryParameter<Int>("senderUserId") { description = "Filter by sender user ID" }
                    queryParameter<Int>("receiverUserId") { description = "Filter by receiver user ID" }
                    queryParameter<String>("status") { description = "pending, accepted, rejected, cancelled" }
                    queryParameter<Boolean>("isDeleted") { description = "Filter by deleted status" }
                }
                response {
                    HttpStatusCode.OK to { body<List<ContactInvitation>>() }
                    HttpStatusCode.NotFound to { description = "Contact invitations not found" }
                }
            }) {
                authGuard.requireUserId(call) ?: return@get
                val filter = ContactInvitationFilter(
                    senderUserId = call.request.queryParameters["senderUserId"]?.toIntOrNull(),
                    receiverUserId = call.request.queryParameters["receiverUserId"]?.toIntOrNull(),
                    status = InvitationStatusEnum.normalize(call.request.queryParameters["status"]),
                    isDeleted = call.request.queryParameters["isDeleted"]?.toBoolean() ?: false
                )

                val invitations = service.findByFilter(filter)
                if (invitations.isNotEmpty()) call.respond(HttpStatusCode.OK, invitations)
                else call.respond(HttpStatusCode.NotFound, "Contact invitations not found")
            }

            get("/{id}", {
                summary = "Get contact invitation by ID"
                request { pathParameter<Int>("id") }
            }) {
                authGuard.requireUserId(call) ?: return@get
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val invitation = service.findById(id)
                if (invitation != null) call.respond(HttpStatusCode.OK, invitation)
                else call.respond(HttpStatusCode.NotFound, "Contact invitation not found")
            }

            post({
                summary = "Create contact invitation"
                response { HttpStatusCode.Created to { description = "Contact invitation created" } }
            }) {
                val currentUserId = authGuard.requireUserId(call) ?: return@post
                val body = call.receive<CreateContactInvitationBodyDTO>()
                runCatching {
                    service.create(
                        ContactInvitation(
                            senderUserId = currentUserId,
                            receiverUserId = body.receiverUserId,
                            message = body.message,
                            createdAt = Instant.now().toString()
                        )
                    )
                }
                    .onSuccess { newId -> call.respond(HttpStatusCode.Created, mapOf("id" to newId)) }
                    .onFailure { call.respond(HttpStatusCode.BadRequest, it.message ?: "Invalid contact invitation data") }
            }

            post("/{id}/accept", {
                summary = "Accept contact invitation"
                request { pathParameter<Int>("id") }
            }) {
                val currentUserId = authGuard.requireUserId(call) ?: return@post
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null && service.accept(id, currentUserId)) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }

            post("/{id}/reject", {
                summary = "Reject contact invitation"
                request { pathParameter<Int>("id") }
            }) {
                val currentUserId = authGuard.requireUserId(call) ?: return@post
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null && service.reject(id, currentUserId)) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }

            post("/{id}/cancel", {
                summary = "Cancel contact invitation"
                request { pathParameter<Int>("id") }
            }) {
                val currentUserId = authGuard.requireUserId(call) ?: return@post
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null && service.cancel(id, currentUserId)) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }

            delete("/{id}", {
                summary = "Soft delete contact invitation"
                request { pathParameter<Int>("id") }
            }) {
                authGuard.requireUserId(call) ?: return@delete
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null && service.softDelete(id)) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
