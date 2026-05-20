package Web.Endpoints

import ChatMembers.DTO.ChatMember
import ChatMembers.DTO.ChatMemberFilter
import ChatMembers.Services.ChatMemberService
import Chats.DTO.Chat
import Chats.Services.ChatService
import Contacts.DTO.ContactFilter
import Contacts.Services.ContactService
import Invitations.DTO.ChatInvitation
import Invitations.DTO.ChatInvitationFilter
import Invitations.DTO.ContactInvitation
import Invitations.DTO.ContactInvitationFilter
import Invitations.Enums.InvitationStatusEnum
import Invitations.Services.ChatInvitationService
import Invitations.Services.ContactInvitationService
import Messages.DTO.Message
import Messages.DTO.MessageFilter
import Messages.Enum.MessageTypeEnum
import Messages.Services.MessageService
import Web.DTO.CreateChatEndpointDTO
import Tokens.Services.AuthGuard
import Users.DTO.UserFilter
import Web.DTO.CreateChatInvitationEndpointDTO
import Web.DTO.CreateContactInvitationEndpointDTO
import Web.DTO.CreateMessageEndpointDTO
import Web.DTO.UpdateProfileEndpointDTO
import Web.Services.DesignSettingsService
import com.example.Users.Services.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import java.time.Instant

fun Application.WebEndpointRouting() {
    val authGuard = AuthGuard()
    val userService = UserService(environment)
    val contactService = ContactService()
    val contactInvitationService = ContactInvitationService()
    val chatInvitationService = ChatInvitationService()
    val chatMemberService = ChatMemberService(environment)
    val chatService = ChatService()
    val messageService = MessageService()
    val designSettingsService = DesignSettingsService(environment)

    suspend fun userById(id: Int) = userService.findById(id)?.toEndpointDTO()
    suspend fun activeMember(chatId: Int, userId: Int) = chatMemberService.findByFilter(
        ChatMemberFilter(idChat = chatId, idUser = userId, isDeleted = false)
    ).filterNotNull().firstOrNull()

    suspend fun activeMembers(chatId: Int) = chatMemberService.findByFilter(
        ChatMemberFilter(idChat = chatId, isDeleted = false)
    ).filterNotNull()

    suspend fun requireActiveMember(chatId: Int, userId: Int): ChatMember? {
        val chat = chatService.findById(chatId)
        if (chat == null || chat.deletedAt != null) return null
        return activeMember(chatId, userId)
    }

    routing {
        route("/web") {
            get("/design-settings") {
                call.respond(HttpStatusCode.OK, designSettingsService.getColors())
            }

            get("/me") {
                val currentUserId = authGuard.requireUserId(call) ?: return@get
                val user = userById(currentUserId)
                if (user != null) call.respond(HttpStatusCode.OK, user)
                else call.respond(HttpStatusCode.NotFound, "User not found")
            }

            put("/me") {
                val currentUserId = authGuard.requireUserId(call) ?: return@put
                val currentUser = userService.findById(currentUserId)
                if (currentUser == null) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@put
                }

                val body = call.receive<UpdateProfileEndpointDTO>()
                val updated = userService.update(
                    currentUserId,
                    currentUser.copy(
                        name = body.name,
                        email = body.email,
                        phone = body.phone,
                        fio = body.fio
                    )
                )

                if (updated) call.respond(HttpStatusCode.OK, userById(currentUserId) ?: "")
                else call.respond(HttpStatusCode.NotFound)
            }

            get("/users") {
                authGuard.requireUserId(call) ?: return@get
                val users = userService.findByFilter(
                    UserFilter(
                        login = call.request.queryParameters["login"],
                        name = call.request.queryParameters["name"],
                        isDeleted = false
                    )
                ).filterNotNull().mapNotNull { it.toEndpointDTO() }

                if (users.isNotEmpty()) call.respond(HttpStatusCode.OK, users)
                else call.respond(HttpStatusCode.NotFound, "Users not found")
            }

            get("/contacts") {
                val currentUserId = authGuard.requireUserId(call) ?: return@get
                val contacts = contactService.findByFilter(
                    ContactFilter(
                        ownerUserId = currentUserId,
                        isDeleted = false
                    )
                ).filterNotNull().mapNotNull { contact ->
                    contact.toEndpointDTO(userById(contact.contactUserId))
                }

                if (contacts.isNotEmpty()) call.respond(HttpStatusCode.OK, contacts)
                else call.respond(HttpStatusCode.NotFound, "Contacts not found")
            }

            route("/chats") {
                get {
                    val currentUserId = authGuard.requireUserId(call) ?: return@get
                    val memberships = chatMemberService.findByFilter(
                        ChatMemberFilter(idUser = currentUserId, isDeleted = false)
                    ).filterNotNull()

                    val chats = memberships.mapNotNull { member ->
                        val chat = chatService.findById(member.idChat) ?: return@mapNotNull null
                        if (chat.deletedAt != null) return@mapNotNull null
                        chat.toEndpointDTO(userById(chat.owner), member.id)
                    }

                    if (chats.isNotEmpty()) call.respond(HttpStatusCode.OK, chats)
                    else call.respond(HttpStatusCode.NotFound, "Chats not found")
                }

                post {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val body = call.receive<CreateChatEndpointDTO>()
                    val name = body.name.trim()

                    if (name.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Chat name is required")
                        return@post
                    }

                    runCatching {
                        val now = Instant.now().toString()
                        val chatId = chatService.create(
                            Chat(name = name, owner = currentUserId, createdAt = now)
                        ) ?: error("Chat was not created")

                        chatMemberService.create(
                            ChatMember(
                                idChat = chatId,
                                idRole = body.idRole,
                                idUser = currentUserId,
                                createdAt = now
                            )
                        )

                        chatService.findById(chatId)?.toEndpointDTO(
                            ownerUser = userById(currentUserId),
                            currentUserMemberId = activeMember(chatId, currentUserId)?.id
                        ) ?: error("Created chat was not found")
                    }
                        .onSuccess { chat -> call.respond(HttpStatusCode.Created, chat) }
                        .onFailure {
                            call.respond(HttpStatusCode.BadRequest, it.message ?: "Invalid chat data")
                        }
                }

                get("/{id}/messages") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@get
                    val chatId = call.parameters["id"]?.toIntOrNull()
                    if (chatId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid chat ID")
                        return@get
                    }

                    if (requireActiveMember(chatId, currentUserId) == null) {
                        call.respond(HttpStatusCode.Forbidden, "Chat is not available")
                        return@get
                    }

                    val members = activeMembers(chatId)
                        .mapNotNull { member -> member.id?.let { memberId -> memberId to member } }
                        .toMap()
                    val messages = members.values.flatMap { member ->
                        messageService.findByFilter(
                            MessageFilter(idChatMember = member.id, isDeleted = false)
                        ).filterNotNull()
                    }.sortedBy { it.createdAt }
                        .mapNotNull { message ->
                            val member = members[message.idChatMember] ?: return@mapNotNull null
                            message.toEndpointDTO(
                                member = member,
                                sender = userById(member.idUser),
                                currentUserId = currentUserId
                            )
                        }

                    call.respond(HttpStatusCode.OK, messages)
                }

                post("/{id}/messages") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val chatId = call.parameters["id"]?.toIntOrNull()
                    if (chatId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid chat ID")
                        return@post
                    }

                    val member = requireActiveMember(chatId, currentUserId)
                    if (member == null) {
                        call.respond(HttpStatusCode.Forbidden, "Chat is not available")
                        return@post
                    }

                    val memberId = member.id
                    if (memberId == null) {
                        call.respond(HttpStatusCode.InternalServerError)
                        return@post
                    }

                    val body = call.receive<CreateMessageEndpointDTO>()
                    val value = body.value.trim()
                    if (value.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Message text is required")
                        return@post
                    }

                    val type = MessageTypeEnum.getEnumByType(body.type.lowercase())
                    if (type == null) {
                        call.respond(HttpStatusCode.BadRequest, "Unsupported message type")
                        return@post
                    }

                    val newId = messageService.create(
                        Message(
                            idChatMember = memberId,
                            value = value,
                            type = type,
                            createdAt = Instant.now().toString()
                        )
                    )

                    val message = newId?.let { messageService.findById(it) }
                    if (message == null) {
                        call.respond(HttpStatusCode.InternalServerError)
                        return@post
                    }

                    call.respond(
                        HttpStatusCode.Created,
                        message.toEndpointDTO(
                            member = member,
                            sender = userById(currentUserId),
                            currentUserId = currentUserId
                        ) ?: mapOf("id" to newId)
                    )
                }
            }

            route("/contact-invitations") {
                get {
                    val currentUserId = authGuard.requireUserId(call) ?: return@get
                    val direction = call.request.queryParameters["direction"]
                    val status = InvitationStatusEnum.normalize(call.request.queryParameters["status"])
                        ?: InvitationStatusEnum.PENDING.value

                    val filter = when (direction) {
                        "outgoing" -> ContactInvitationFilter(
                            senderUserId = currentUserId,
                            status = status,
                            isDeleted = false
                        )

                        else -> ContactInvitationFilter(
                            receiverUserId = currentUserId,
                            status = status,
                            isDeleted = false
                        )
                    }

                    val invitations = contactInvitationService.findByFilter(filter)
                        .filterNotNull()
                        .mapNotNull { invitation ->
                            invitation.toEndpointDTO(
                                sender = userById(invitation.senderUserId),
                                receiver = userById(invitation.receiverUserId)
                            )
                        }

                    if (invitations.isNotEmpty()) call.respond(HttpStatusCode.OK, invitations)
                    else call.respond(HttpStatusCode.NotFound, "Contact invitations not found")
                }

                post {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val body = call.receive<CreateContactInvitationEndpointDTO>()
                    runCatching {
                        contactInvitationService.create(
                            ContactInvitation(
                                senderUserId = currentUserId,
                                receiverUserId = body.receiverUserId,
                                message = body.message,
                                createdAt = Instant.now().toString()
                            )
                        )
                    }
                        .onSuccess { newId -> call.respond(HttpStatusCode.Created, mapOf("id" to newId)) }
                        .onFailure {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                it.message ?: "Invalid contact invitation data"
                            )
                        }
                }

                post("/{id}/accept") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null && contactInvitationService.accept(id, currentUserId)) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }

                post("/{id}/reject") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null && contactInvitationService.reject(id, currentUserId)) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }

                post("/{id}/cancel") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null && contactInvitationService.cancel(id, currentUserId)) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }
            }

            route("/chat-invitations") {
                get {
                    val currentUserId = authGuard.requireUserId(call) ?: return@get
                    val direction = call.request.queryParameters["direction"]
                    val status = InvitationStatusEnum.normalize(call.request.queryParameters["status"])
                        ?: InvitationStatusEnum.PENDING.value

                    val filter = when (direction) {
                        "outgoing" -> ChatInvitationFilter(
                            inviterUserId = currentUserId,
                            status = status,
                            isDeleted = false
                        )

                        else -> ChatInvitationFilter(
                            inviteeUserId = currentUserId,
                            status = status,
                            isDeleted = false
                        )
                    }

                    val invitations = chatInvitationService.findByFilter(filter)
                        .filterNotNull()
                        .mapNotNull { invitation ->
                            val sourceChat = chatService.findById(invitation.idChat)
                            val chat = sourceChat?.toEndpointDTO(userById(sourceChat.owner))

                            invitation.toEndpointDTO(
                                chat = chat,
                                inviter = userById(invitation.inviterUserId),
                                invitee = userById(invitation.inviteeUserId)
                            )
                        }

                    if (invitations.isNotEmpty()) call.respond(HttpStatusCode.OK, invitations)
                    else call.respond(HttpStatusCode.NotFound, "Chat invitations not found")
                }

                post {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val body = call.receive<CreateChatInvitationEndpointDTO>()
                    val chat = chatService.findById(body.idChat)
                    if (chat == null || chat.deletedAt != null || activeMember(body.idChat, currentUserId) == null) {
                        call.respond(HttpStatusCode.Forbidden, "Chat is not available")
                        return@post
                    }

                    if (activeMember(body.idChat, body.inviteeUserId) != null) {
                        call.respond(HttpStatusCode.BadRequest, "User is already in chat")
                        return@post
                    }

                    runCatching {
                        chatInvitationService.create(
                            ChatInvitation(
                                idChat = body.idChat,
                                inviterUserId = currentUserId,
                                inviteeUserId = body.inviteeUserId,
                                idRole = body.idRole,
                                message = body.message,
                                createdAt = Instant.now().toString()
                            )
                        )
                    }
                        .onSuccess { newId -> call.respond(HttpStatusCode.Created, mapOf("id" to newId)) }
                        .onFailure {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                it.message ?: "Invalid chat invitation data"
                            )
                        }
                }

                post("/{id}/accept") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null && chatInvitationService.accept(id, currentUserId)) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }

                post("/{id}/reject") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null && chatInvitationService.reject(id, currentUserId)) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }

                post("/{id}/cancel") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null && chatInvitationService.cancel(id, currentUserId)) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
