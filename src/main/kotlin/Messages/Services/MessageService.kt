package Messages.Services

import Messages.DTO.Message
import Messages.DTO.MessageFilter
import Messages.Repositories.MessageRepository
import java.time.Instant

class MessageService {
    private val repository = MessageRepository()

    suspend fun findById(id: Int): Message? = repository.findById(id)

    suspend fun findByFilter(filter: MessageFilter): List<Message?> = repository.findByFilter(filter)

    suspend fun create(message: Message): Int? {
        repository.create(message)
        return repository.findByFilter(
            MessageFilter(idChatMember = message.idChatMember, value = message.value)
        ).lastOrNull()?.id
    }

    suspend fun update(id: Int, message: Message): Boolean {
        if (repository.findById(id) != null) {
            repository.updateById(id, message)
            return true
        }
        return false
    }

    suspend fun softDelete(id: Int): Boolean {
        val message = repository.findById(id)
        if (message != null) {
            val deletedMessage = message.copy(deletedAt = Instant.now().toString())
            repository.updateById(id, deletedMessage)
            return true
        }
        return false
    }

    suspend fun markAsViewed(id: Int): Boolean {
        val message = repository.findById(id)
        if (message != null && message.viewedAt == null) {
            repository.updateById(id, message.copy(viewedAt = Instant.now().toString()))
            return true
        }
        return false
    }
}