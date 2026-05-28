package Web.Endpoints

import ChatMembers.DTO.ChatMember
import ChatMembers.DTO.ChatMemberFilter
import ChatMembers.Services.ChatMemberService
import Chats.Services.ChatAccessService
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
import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Repositories.LogRepository
import Logger.Services.AuditLogWriter
import Media.Repositories.MessageAttachmentRepository
import Media.Services.MediaService
import Media.Services.MediaValidationException
import Messages.DTO.MessageFilter
import Messages.Services.MessageDeliveryException
import Messages.Services.MessageDeliveryService
import Messages.Services.MessageService
import Passwords.Services.PasswordService
import Roles.Constants.ChatRoleNames
import Roles.Services.RoleService
import Sockets.SocketBroadcaster
import Web.DTO.ChangePasswordEndpointDTO
import Web.DTO.ChatRoleEndpointDTO
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
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.*
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun Application.WebEndpointRouting() {
    val authGuard = AuthGuard()
    val userService = UserService(environment)
    val contactService = ContactService()
    val contactInvitationService = ContactInvitationService()
    val chatInvitationService = ChatInvitationService()
    val chatMemberService = ChatMemberService(environment)
    val chatService = ChatService(environment)
    val chatAccessService = ChatAccessService(environment)
    val messageService = MessageService()
    val messageDeliveryService = MessageDeliveryService(environment)
    val mediaService = MediaService(environment)
    val attachmentRepository = MessageAttachmentRepository()
    val designSettingsService = DesignSettingsService(environment)
    val passwordService = PasswordService()
    val roleService = RoleService()
    val logRepository = LogRepository()

    suspend fun userById(id: Int) = userService.findById(id)?.toEndpointDTO()
    suspend fun requireAdminUserId(call: ApplicationCall): Int? {
        val currentUserId = authGuard.requireUserId(call) ?: return null
        val user = userService.findById(currentUserId)
        if (user?.isAdmin != true) {
            call.respond(HttpStatusCode.Forbidden, "Admin access required")
            return null
        }
        return currentUserId
    }

    suspend fun audit(call: ApplicationCall, userId: Int?, type: LogType, event: EventType, description: String) {
        AuditLogWriter.write(
            userId = userId,
            type = type,
            event = event,
            ipAddress = call.request.origin.remoteAddress,
            description = description
        )
    }

    fun logDateValue(value: String): Instant? =
        runCatching { Instant.parse(value) }.getOrNull()
            ?: runCatching { LocalDateTime.parse(value).toInstant(ZoneOffset.UTC) }.getOrNull()

    fun requestDateValue(value: String?): Instant? =
        value?.takeIf { it.isNotBlank() }?.let {
            runCatching {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toInstant(ZoneOffset.UTC)
            }.getOrNull()
        }
    suspend fun activeMember(chatId: Int, userId: Int) = chatAccessService.activeMember(chatId, userId)

    suspend fun activeMembers(chatId: Int) = chatAccessService.activeMembers(chatId)

    suspend fun messagesByMembers(members: List<ChatMember>) = members.flatMap { member ->
        messageService.findByFilter(
            MessageFilter(idChatMember = member.id, isDeleted = false)
        ).filterNotNull()
    }.sortedBy { it.createdAt }

    suspend fun unreadCountFor(currentUserId: Int, members: List<ChatMember>): Int {
        val membersById = members.mapNotNull { member -> member.id?.let { it to member } }.toMap()
        return messagesByMembers(members).count { message ->
            val sender = membersById[message.idChatMember] ?: return@count false
            sender.idUser != currentUserId && message.viewedAt == null
        }
    }

    suspend fun participantUsers(members: List<ChatMember>) = members.mapNotNull { member ->
        userById(member.idUser)
    }

    suspend fun contactInvitationById(id: Int) = contactInvitationService.findById(id)?.let { invitation ->
        invitation.toEndpointDTO(
            sender = userById(invitation.senderUserId),
            receiver = userById(invitation.receiverUserId)
        )
    }

    suspend fun chatInvitationById(id: Int) = chatInvitationService.findById(id)?.let { invitation ->
        val sourceChat = chatService.findById(invitation.idChat)
        val chat = sourceChat?.toEndpointDTO(userById(sourceChat.owner))

        invitation.toEndpointDTO(
            chat = chat,
            inviter = userById(invitation.inviterUserId),
            invitee = userById(invitation.inviteeUserId)
        )
    }

    suspend fun requireActiveMember(chatId: Int, userId: Int): ChatMember? =
        chatAccessService.requireActiveMember(chatId, userId)

    routing {
        route("/web") {
            get("/design-settings") {
                call.respond(HttpStatusCode.OK, designSettingsService.getColors())
            }

            get("/admin/audit-logs") {
                requireAdminUserId(call) ?: return@get
                val type = call.request.queryParameters["type"]?.takeIf { it.isNotBlank() }
                val event = call.request.queryParameters["event"]?.takeIf { it.isNotBlank() }
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()
                val ipAddress = call.request.queryParameters["ip"]?.takeIf { it.isNotBlank() }
                val dateFrom = requestDateValue(call.request.queryParameters["dateFrom"])
                val dateTo = requestDateValue(call.request.queryParameters["dateTo"])

                val logs = logRepository.findAll()
                    .filterNotNull()
                    .filter { type == null || it.logType == type }
                    .filter { event == null || it.event == event }
                    .filter { userId == null || it.idUser == userId }
                    .filter { ipAddress == null || it.ipAddress.contains(ipAddress, ignoreCase = true) }
                    .filter { log ->
                        val logDate = logDateValue(log.date) ?: return@filter true
                        (dateFrom == null || !logDate.isBefore(dateFrom)) &&
                            (dateTo == null || !logDate.isAfter(dateTo))
                    }
                    .sortedByDescending { it.date }
                    .mapNotNull { it.toEndpointDTO() }

                call.respond(HttpStatusCode.OK, logs)
            }

            get("/admin/audit-options") {
                requireAdminUserId(call) ?: return@get
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "types" to LogType.entries.map { it.name },
                        "events" to EventType.entries.map { it.name }
                    )
                )
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

            put("/me/password") {
                val currentUserId = authGuard.requireUserId(call) ?: return@put
                val currentUser = userService.findById(currentUserId)
                if (currentUser == null) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@put
                }

                val body = call.receive<ChangePasswordEndpointDTO>()
                if (!passwordService.checkHashPassword(currentUser.passwordHash, body.currentPassword)) {
                    call.respond(HttpStatusCode.BadRequest, "Current password is invalid")
                    return@put
                }

                if (body.newPassword.length < 8) {
                    call.respond(HttpStatusCode.BadRequest, "New password is too short")
                    return@put
                }

                val updated = userService.update(
                    currentUserId,
                    currentUser.copy(passwordHash = passwordService.createHashPassword(body.newPassword))
                )

                if (updated) {
                    audit(call, currentUserId, LogType.Event, EventType.USER_CHANGE_PASSWORD, "User changed password")
                    call.respond(HttpStatusCode.OK)
                }
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

            get("/users/{id}/public") {
                authGuard.requireUserId(call) ?: return@get
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                    return@get
                }

                val user = userById(id)
                if (user != null) call.respond(HttpStatusCode.OK, user)
                else call.respond(HttpStatusCode.NotFound, "User not found")
            }

            get("/chat-roles") {
                authGuard.requireUserId(call) ?: return@get
                val roleNames = listOf(
                    ChatRoleNames.PARTICIPANT,
                    ChatRoleNames.MODERATOR,
                    ChatRoleNames.ADMINISTRATOR
                )
                val roles = roleNames.map { name ->
                    val role = roleService.requireActiveByName(name)
                    ChatRoleEndpointDTO(role.id ?: 0, role.name)
                }

                call.respond(HttpStatusCode.OK, roles)
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

            delete("/contacts/{id}") {
                val currentUserId = authGuard.requireUserId(call) ?: return@delete
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid contact ID")
                    return@delete
                }

                val contact = contactService.findById(id)
                if (contact == null || contact.ownerUserId != currentUserId) {
                    call.respond(HttpStatusCode.NotFound, "Contact not found")
                    return@delete
                }

                if (contactService.softDelete(id)) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound)
            }

            route("/media") {
                post {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    var uploaded = false

                    runCatching {
                        val multipart = call.receiveMultipart()
                        var response: Web.DTO.MediaFileEndpointDTO? = null

                        multipart.forEachPart { part ->
                            try {
                                if (part is PartData.FileItem && !uploaded) {
                                    response = mediaService.saveUploadedFile(currentUserId, part)
                                    uploaded = true
                                }
                            } finally {
                                part.dispose()
                            }
                        }

                        response ?: throw MediaValidationException("MEDIA_FILE_REQUIRED", "Media file is required")
                    }.onSuccess { media ->
                        call.respond(HttpStatusCode.Created, media)
                    }.onFailure { error ->
                        val message = error.message ?: "Invalid media file"
                        audit(call, currentUserId, LogType.Error, EventType.ERROR_STORAGE, message)
                        call.respond(HttpStatusCode.BadRequest, message)
                    }
                }

                get("/{id}/content") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@get
                    val mediaId = call.parameters["id"]?.toIntOrNull()
                    if (mediaId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid media ID")
                        return@get
                    }

                    val media = mediaService.findById(mediaId)
                    if (media == null || media.deletedAt != null) {
                        call.respond(HttpStatusCode.NotFound, "Media file not found")
                        return@get
                    }

                    val hasAccess = media.uploaderUserId == currentUserId ||
                        attachmentRepository.hasUserAccessToMedia(currentUserId, mediaId)
                    if (!hasAccess) {
                        call.respond(HttpStatusCode.Forbidden, "Media file is not available")
                        return@get
                    }

                    val file = File(media.storagePath)
                    if (!file.exists()) {
                        call.respond(HttpStatusCode.NotFound, "Media content not found")
                        return@get
                    }

                    call.respondFile(file)
                }
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
                        val members = activeMembers(member.idChat)
                        chat.toEndpointDTO(
                            ownerUser = userById(chat.owner),
                            currentUserMemberId = member.id,
                            participants = participantUsers(members),
                            unreadCount = unreadCountFor(currentUserId, members)
                        )
                    }

                    if (chats.isNotEmpty()) call.respond(HttpStatusCode.OK, chats)
                    else call.respond(HttpStatusCode.NotFound, "Chats not found")
                }

                get("/direct/{contactUserId}") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@get
                    val contactUserId = call.parameters["contactUserId"]?.toIntOrNull()
                    if (contactUserId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid contact user ID")
                        return@get
                    }

                    val currentMemberships = chatMemberService.findByFilter(
                        ChatMemberFilter(idUser = currentUserId, isDeleted = false)
                    ).filterNotNull()
                    val contactMemberships = chatMemberService.findByFilter(
                        ChatMemberFilter(idUser = contactUserId, isDeleted = false)
                    ).filterNotNull()
                    val contactChatIds = contactMemberships.map { it.idChat }.toSet()

                    val directChat = currentMemberships.firstNotNullOfOrNull { member ->
                        if (!contactChatIds.contains(member.idChat)) return@firstNotNullOfOrNull null
                        val members = activeMembers(member.idChat)
                        if (members.size != 2) return@firstNotNullOfOrNull null
                        val chat = chatService.findById(member.idChat) ?: return@firstNotNullOfOrNull null
                        if (chat.deletedAt != null) return@firstNotNullOfOrNull null
                        chat.toEndpointDTO(
                            ownerUser = userById(chat.owner),
                            currentUserMemberId = member.id,
                            participants = participantUsers(members),
                            unreadCount = unreadCountFor(currentUserId, members)
                        )
                    }

                    if (directChat != null) call.respond(HttpStatusCode.OK, directChat)
                    else call.respond(HttpStatusCode.NotFound, "Direct chat not found")
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

                        chatService.findById(chatId)?.toEndpointDTO(
                            ownerUser = userById(currentUserId),
                            currentUserMemberId = activeMember(chatId, currentUserId)?.id,
                            participants = participantUsers(activeMembers(chatId)),
                            unreadCount = 0
                        ) ?: error("Created chat was not found")
                    }
                        .onSuccess { chat ->
                            audit(call, currentUserId, LogType.Event, EventType.NEW_CHAT, "Created chat ${chat.id}: ${chat.name}")
                            call.respond(HttpStatusCode.Created, chat)
                        }
                        .onFailure {
                            audit(call, currentUserId, LogType.Error, EventType.ERROR_MESSAGE, it.message ?: "Invalid chat data")
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
                    val rawMessages = messagesByMembers(members.values.toList())
                    val attachmentsByMessage = attachmentRepository
                        .findByMessageIds(rawMessages.mapNotNull { it.id })
                        .groupBy({ it.first.idMessage }, { it.second.toEndpointDTO() })

                    val messages = rawMessages.mapNotNull { message ->
                            val member = members[message.idChatMember] ?: return@mapNotNull null
                            message.toEndpointDTO(
                                member = member,
                                sender = userById(member.idUser),
                                currentUserId = currentUserId,
                                attachments = message.id?.let { attachmentsByMessage[it] }.orEmpty()
                            )
                        }

                    rawMessages
                        .filter { message ->
                            val member = members[message.idChatMember] ?: return@filter false
                            member.idUser != currentUserId && message.viewedAt == null
                        }
                        .mapNotNull { it.id }
                        .forEach { messageService.markAsViewed(it) }

                    call.respond(HttpStatusCode.OK, messages)
                }

                post("/{id}/messages") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val chatId = call.parameters["id"]?.toIntOrNull()
                    if (chatId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid chat ID")
                        return@post
                    }

                    val body = call.receive<CreateMessageEndpointDTO>()
                    runCatching {
                        messageDeliveryService.createForChat(
                            chatId = chatId,
                            senderUserId = currentUserId,
                            value = body.value,
                            type = body.type,
                            mediaFileIds = body.mediaFileIds
                        )
                    }.onSuccess { result ->
                        audit(
                            call,
                            currentUserId,
                            LogType.Event,
                            EventType.NEW_MESSAGE,
                            "Sent message ${result.endpointMessage.id} to chat $chatId"
                        )
                        SocketBroadcaster.messageCreated(result)
                        call.respond(HttpStatusCode.Created, result.endpointMessage)
                    }.onFailure { error ->
                        audit(call, currentUserId, LogType.Error, EventType.ERROR_MESSAGE, error.message ?: "Message delivery failed")
                        if (error is MessageDeliveryException) {
                            val status = when (error.code) {
                                "CHAT_ACCESS_DENIED" -> HttpStatusCode.Forbidden
                                "MESSAGE_CREATE_FAILED",
                                "MESSAGE_NOT_FOUND",
                                "MESSAGE_MAPPING_FAILED",
                                "INVALID_CHAT_MEMBER" -> HttpStatusCode.InternalServerError
                                else -> HttpStatusCode.BadRequest
                            }
                            call.respond(status, error.message)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }
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
                        .onSuccess { newId ->
                            val invitation = newId?.let { contactInvitationById(it) }
                            if (invitation != null) {
                                SocketBroadcaster.contactInvitationCreated(
                                    senderUserId = currentUserId,
                                    receiverUserId = body.receiverUserId,
                                    invitation = invitation
                                )
                            }
                            audit(call, currentUserId, LogType.Event, EventType.ADD_CHAT_MEMBER, "Created contact invitation $newId")
                            call.respond(HttpStatusCode.Created, mapOf("id" to newId))
                        }
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
                    if (id != null && contactInvitationService.accept(id, currentUserId)) {
                        val invitation = contactInvitationById(id)
                        if (invitation != null) {
                            SocketBroadcaster.contactInvitationUpdated(
                                actorUserId = currentUserId,
                                senderUserId = invitation.senderUserId,
                                receiverUserId = invitation.receiverUserId,
                                invitation = invitation
                            )
                        }
                        audit(call, currentUserId, LogType.Event, EventType.ADD_CHAT_MEMBER, "Accepted contact invitation $id")
                        call.respond(HttpStatusCode.OK)
                    } else call.respond(HttpStatusCode.NotFound)
                }

                post("/{id}/reject") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null && contactInvitationService.reject(id, currentUserId)) {
                        val invitation = contactInvitationById(id)
                        if (invitation != null) {
                            SocketBroadcaster.contactInvitationUpdated(
                                actorUserId = currentUserId,
                                senderUserId = invitation.senderUserId,
                                receiverUserId = invitation.receiverUserId,
                                invitation = invitation
                            )
                        }
                        audit(call, currentUserId, LogType.Event, EventType.DELETE_CHAT_MEMBER, "Rejected contact invitation $id")
                        call.respond(HttpStatusCode.OK)
                    } else call.respond(HttpStatusCode.NotFound)
                }

                post("/{id}/cancel") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null && contactInvitationService.cancel(id, currentUserId)) {
                        val invitation = contactInvitationById(id)
                        if (invitation != null) {
                            SocketBroadcaster.contactInvitationUpdated(
                                actorUserId = currentUserId,
                                senderUserId = invitation.senderUserId,
                                receiverUserId = invitation.receiverUserId,
                                invitation = invitation
                            )
                        }
                        call.respond(HttpStatusCode.OK)
                    } else call.respond(HttpStatusCode.NotFound)
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
                                idRole = body.idRole ?: 0,
                                message = body.message,
                                createdAt = Instant.now().toString()
                            )
                        )
                    }
                        .onSuccess { newId ->
                            val invitation = newId?.let { chatInvitationById(it) }
                            if (invitation != null) {
                                SocketBroadcaster.chatInvitationCreated(
                                    senderUserId = currentUserId,
                                    receiverUserId = body.inviteeUserId,
                                    invitation = invitation
                                )
                            }
                            audit(call, currentUserId, LogType.Event, EventType.ADD_CHAT_MEMBER, "Created chat invitation $newId for chat ${body.idChat}")
                            call.respond(HttpStatusCode.Created, mapOf("id" to newId))
                        }
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
                    if (id != null && chatInvitationService.accept(id, currentUserId)) {
                        val invitation = chatInvitationById(id)
                        if (invitation != null) {
                            SocketBroadcaster.chatInvitationUpdated(
                                actorUserId = currentUserId,
                                senderUserId = invitation.inviterUserId,
                                receiverUserId = invitation.inviteeUserId,
                                invitation = invitation
                            )
                        }
                        audit(call, currentUserId, LogType.Event, EventType.ADD_CHAT_MEMBER, "Accepted chat invitation $id")
                        call.respond(HttpStatusCode.OK)
                    } else call.respond(HttpStatusCode.NotFound)
                }

                post("/{id}/reject") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null && chatInvitationService.reject(id, currentUserId)) {
                        val invitation = chatInvitationById(id)
                        if (invitation != null) {
                            SocketBroadcaster.chatInvitationUpdated(
                                actorUserId = currentUserId,
                                senderUserId = invitation.inviterUserId,
                                receiverUserId = invitation.inviteeUserId,
                                invitation = invitation
                            )
                        }
                        audit(call, currentUserId, LogType.Event, EventType.DELETE_CHAT_MEMBER, "Rejected chat invitation $id")
                        call.respond(HttpStatusCode.OK)
                    } else call.respond(HttpStatusCode.NotFound)
                }

                post("/{id}/cancel") {
                    val currentUserId = authGuard.requireUserId(call) ?: return@post
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null && chatInvitationService.cancel(id, currentUserId)) {
                        val invitation = chatInvitationById(id)
                        if (invitation != null) {
                            SocketBroadcaster.chatInvitationUpdated(
                                actorUserId = currentUserId,
                                senderUserId = invitation.inviterUserId,
                                receiverUserId = invitation.inviteeUserId,
                                invitation = invitation
                            )
                        }
                        call.respond(HttpStatusCode.OK)
                    } else call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
