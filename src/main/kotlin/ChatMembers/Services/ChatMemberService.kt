package ChatMembers.Services

import ChatMembers.DTO.ChatMember
import ChatMembers.DTO.ChatMemberFilter
import ChatMembers.Repositories.ChatMemberRepository
import java.time.Instant

class ChatMemberService {
    private val repository = ChatMemberRepository()

    suspend fun findById(id: Int): ChatMember? = repository.findById(id)

    suspend fun findByFilter(filter: ChatMemberFilter): List<ChatMember?> = repository.findByFilter(filter)

    suspend fun create(member: ChatMember): Int? {
        repository.create(member)
        // Поиск созданного участника для возврата ID
        return repository.findByFilter(
            ChatMemberFilter(idChat = member.idChat, idUser = member.idUser)
        ).firstOrNull()?.id
    }

    suspend fun update(id: Int, member: ChatMember): Boolean {
        if (repository.findById(id) != null) {
            repository.updateById(id, member)
            return true
        }
        return false
    }

    suspend fun softDelete(id: Int): Boolean {
        val member = repository.findById(id)
        if (member != null) {
            val deletedMember = member.copy(deletedAt = Instant.now().toString())
            repository.updateById(id, deletedMember)
            return true
        }
        return false
    }
}