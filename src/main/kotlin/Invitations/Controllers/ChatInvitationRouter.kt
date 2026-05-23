package Invitations.Controllers

import Invitations.DTO.ChatInvitation
import Invitations.DTO.ChatInvitationFilter
import Invitations.DTO.CreateChatInvitationBodyDTO
import Invitations.Enums.InvitationStatusEnum
import Invitations.Services.ChatInvitationService
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

fun Application.ChatInvitationRouting() {
    val service = ChatInvitationService()
    val authGuard = AuthGuard()

    routing {
        route("/chat-invitations", { tags = listOf("chat_invitations") }) {
            get({
                summary = "Get chat invitations by filter"
                request {
                    queryParameter<Int>("idChat") { description = "Filter by chat ID" }
                    queryParameter<Int>("inviterUserId") { description = "Filter by inviter user ID" }
                    queryParameter<Int>("inviteeUserId") { description = "Filter by invitee user ID" }
                    queryParameter<String>("status") { description = "pending, accepted, rejected, cancelled" }
                    queryParameter<Boolean>("isDeleted") { description = "Filter by deleted status" }
                }
                response {
                    HttpStatusCode.OK to { body<List<ChatInvitation>>() }
                    HttpStatusCode.NotFound to { description = "Chat invitations not found" }
                }
            }) {
                authGuard.requireUserId(call) ?: return@get
                val filter = ChatInvitationFilter(
                    idChat = call.request.queryParameters["idChat"]?.toIntOrNull(),
                    inviterUserId = call.request.queryParameters["inviterUserId"]?.toIntOrNull(),
                    inviteeUserId = call.request.queryParameters["inviteeUserId"]?.toIntOrNull(),
                    status = InvitationStatusEnum.normalize(call.request.queryParameters["status"]),
                    isDeleted = call.request.queryParameters["isDeleted"]?.toBoolean() ?: false
                )

                val invitations = service.findByFilter(filter)
                if (invitations.isNotEmpty()) call.respond(HttpStatusCode.OK, invitations)
                else call.respond(HttpStatusCode.NotFound, "Chat invitations not found")
            }

            get("/{id}", {
                summary = "Get chat invitation by ID"
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
                else call.respond(HttpStatusCode.NotFound, "Chat invitation not found")
            }

            post({
                summary = "Create chat invitation"
                response { HttpStatusCode.Created to { description = "Chat invitation created" } }
            }) {
                val currentUserId = authGuard.requireUserId(call) ?: return@post
                val body = call.receive<CreateChatInvitationBodyDTO>()
                runCatching {
                    service.create(
                        ChatInvitation(
                            idChat = body.idChat,
                            inviterUserId = currentUserId,
                            inviteeUserId = body.inviteeUserId,
                            message = body.message,
                            createdAt = Instant.now().toString()
                        )
                    )
                }
                    .onSuccess { newId -> call.respond(HttpStatusCode.Created, mapOf("id" to newId)) }
                    .onFailure { call.respond(HttpStatusCode.BadRequest, it.message ?: "Invalid chat invitation data") }
            }

            post("/{id}/accept", {
                summary = "Accept chat invitation"
                request { pathParameter<Int>("id") }
            }) {
                val currentUserId = authGuard.requireUserId(call) ?: return@post
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null && service.accept(id, currentUserId)) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }

            post("/{id}/reject", {
                summary = "Reject chat invitation"
                request { pathParameter<Int>("id") }
            }) {
                val currentUserId = authGuard.requireUserId(call) ?: return@post
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null && service.reject(id, currentUserId)) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }

            post("/{id}/cancel", {
                summary = "Cancel chat invitation"
                request { pathParameter<Int>("id") }
            }) {
                val currentUserId = authGuard.requireUserId(call) ?: return@post
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null && service.cancel(id, currentUserId)) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }

            delete("/{id}", {
                summary = "Soft delete chat invitation"
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
