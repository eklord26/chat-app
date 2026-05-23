package Chats.Services

import ChatMembers.DTO.ChatMember
import ChatMembers.Services.ChatMemberService
import Chats.DTO.Chat
import Chats.DTO.ChatFilter
import Chats.Repositories.ChatRepository
import Roles.Services.RoleService
import io.ktor.server.application.ApplicationEnvironment
import java.time.Instant

class ChatService(environment: ApplicationEnvironment? = null) {
    private val repository = ChatRepository()
    private val chatMemberService = ChatMemberService(environment)
    private val roleService = RoleService()

    suspend fun findById(id: Int): Chat? = repository.findById(id)

    suspend fun findByFilter(filter: ChatFilter): List<Chat?> = repository.findByFilter(filter)

    suspend fun create(chat: Chat): Int? {
        repository.create(chat)
        val chatId = repository.findByFilter(ChatFilter(name = chat.name, owner = chat.owner))
            .lastOrNull()?.id

        if (chatId != null) {
            chatMemberService.create(
                ChatMember(
                    idChat = chatId,
                    idRole = roleService.administratorRoleId(),
                    idUser = chat.owner,
                    createdAt = Instant.now().toString()
                )
            )
        }

        return chatId
    }

    suspend fun update(id: Int, chat: Chat): Boolean {
        if (repository.findById(id) != null) {
            repository.updateById(id, chat)
            return true
        }
        return false
    }

    suspend fun softDelete(id: Int): Boolean {
        val chat = repository.findById(id)
        if (chat != null) {
            val deletedChat = chat.copy(deletedAt = Instant.now().toString())
            repository.updateById(id, deletedChat)
            return true
        }
        return false
    }
}
