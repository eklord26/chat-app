package Contacts.Controllers

import Contacts.DTO.Contact
import Contacts.DTO.ContactFilter
import Contacts.DTO.CreateContactBodyDTO
import Contacts.Services.ContactService
import Tokens.Services.AuthGuard
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import java.time.Instant

fun Application.ContactRouting() {
    val service = ContactService()
    val authGuard = AuthGuard()

    routing {
        route("/contacts", { tags = listOf("contacts") }) {
            get({
                summary = "Get contacts by filter"
                request {
                    queryParameter<Int>("ownerUserId") { description = "Filter by owner user ID" }
                    queryParameter<Int>("contactUserId") { description = "Filter by contact user ID" }
                    queryParameter<Boolean>("isDeleted") { description = "Filter by deleted status" }
                }
                response {
                    HttpStatusCode.OK to { body<List<Contact>>() }
                    HttpStatusCode.NotFound to { description = "Contacts not found" }
                }
            }) {
                val currentUserId = authGuard.requireUserId(call) ?: return@get
                val filter = ContactFilter(
                    ownerUserId = call.request.queryParameters["ownerUserId"]?.toIntOrNull() ?: currentUserId,
                    contactUserId = call.request.queryParameters["contactUserId"]?.toIntOrNull(),
                    isDeleted = call.request.queryParameters["isDeleted"]?.toBoolean() ?: false
                )

                val contacts = service.findByFilter(filter)
                if (contacts.isNotEmpty()) call.respond(HttpStatusCode.OK, contacts)
                else call.respond(HttpStatusCode.NotFound, "Contacts not found")
            }

            get("/{id}", {
                summary = "Get contact by ID"
                request { pathParameter<Int>("id") }
            }) {
                authGuard.requireUserId(call) ?: return@get
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val contact = service.findById(id)
                if (contact != null) call.respond(HttpStatusCode.OK, contact)
                else call.respond(HttpStatusCode.NotFound, "Contact not found")
            }

            post({
                summary = "Create contact for current user"
                response { HttpStatusCode.Created to { description = "Contact created" } }
            }) {
                val currentUserId = authGuard.requireUserId(call) ?: return@post
                val body = call.receive<CreateContactBodyDTO>()
                runCatching {
                    service.create(
                        Contact(
                            ownerUserId = currentUserId,
                            contactUserId = body.contactUserId,
                            displayName = body.displayName,
                            createdAt = Instant.now().toString()
                        )
                    )
                }
                    .onSuccess { newId -> call.respond(HttpStatusCode.Created, mapOf("id" to newId)) }
                    .onFailure { call.respond(HttpStatusCode.BadRequest, it.message ?: "Invalid contact data") }
            }

            put("/{id}", {
                summary = "Update contact"
                request { pathParameter<Int>("id") }
            }) {
                val currentUserId = authGuard.requireUserId(call) ?: return@put
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@put
                }

                val body = call.receive<CreateContactBodyDTO>()
                val updated = service.update(
                    id,
                    Contact(
                        id = id,
                        ownerUserId = currentUserId,
                        contactUserId = body.contactUserId,
                        displayName = body.displayName,
                        createdAt = Instant.now().toString()
                    )
                )
                if (updated) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }

            delete("/{id}", {
                summary = "Soft delete contact"
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
