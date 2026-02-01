package ChatMembers.Controllers

import ChatMembers.DTO.ChatMember
import ChatMembers.DTO.ChatMemberFilter
import ChatMembers.Repositories.ChatMemberRepository
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
fun Application.ChatMemberRouting() {

    val repo = ChatMemberRepository()

    routing {
        route("/members",
            {
                tags = listOf("members")
            })
        {
            get({
                tags = listOf("members")
                summary = "Get members by filter"
                description = "Retrieves a list of members matching the filter criteria"

                request {
                    queryParameter<String>("login") { description = "Filter by exact login" }
                    queryParameter<String>("name") { description = "Filter by partial name" }
                    queryParameter<Boolean>("isAdmin") { description = "Filter by admin status" }
                    queryParameter<Boolean>("deleted") { description = "Filter by deleted status" }
                }

                response {
                    HttpStatusCode.OK to {
                        description = "List of found members"
                        body<List<ChatMember>>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "Member not found"
                    }
                }
            }) {
                val filter = ChatMemberFilter(
                    idChat = call.request.queryParameters["idChat"]?.toInt(),
                    idRole = call.request.queryParameters["name"]?.toInt(),
                    idUser = call.request.queryParameters["name"]?.toInt(),
                    createdAt = call.request.queryParameters["isAdmin"],
                    deletedAt = call.request.queryParameters["deleted"]
                )

                val members = repo.findByFilter(filter)

                if (members.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, members)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Member not found")
                }
            }

            // Метод получения по ID. Путь "/members/{id}"
            get("/{id}", {
                tags = listOf("members")
                summary = "Get member by ID"
                description = "Retrieves detailed information for a specific member"

                request {
                    pathParameter<Int>("id") { description = "Member ID" }
                }

                response {
                    HttpStatusCode.OK to {
                        description = "Member found"
                        body<ChatMember>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "Member not found"
                    }
                }
            }) {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }

                val member = repo.findById(id)
                if (member != null) {
                    call.respond(HttpStatusCode.OK, member)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Member not found")
                }
            }
            post {
                val id = call.request.queryParameters["id"]

                if ((id !== null) && id.toIntOrNull() != null) {
                    val member = ChatMemberRepository().findById(id.toInt())

                    if (member !== null) {
                        call.respond(member)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Member not found")
                    }
                }
            }
            put {

            }
            patch {

            }
            delete("/{id}", {
                operationId = "deleteMemberById"
                summary = "Delete member by ID"
                description = "Delete member by ID"
            }) {

            }
        }
    }
}
