package Chats.Services

import Chats.DTO.Chat
import Chats.DTO.ChatFilter
import Chats.Repositories.ChatRepository
import java.time.Instant

class ChatService {
    private val repository = ChatRepository()

    suspend fun findById(id: Int): Chat? = repository.findById(id)

    suspend fun findByFilter(filter: ChatFilter): List<Chat?> = repository.findByFilter(filter)

    suspend fun create(chat: Chat): Int? {
        repository.create(chat)
        return repository.findByFilter(ChatFilter(name = chat.name, owner = chat.owner))
            .firstOrNull()?.id
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