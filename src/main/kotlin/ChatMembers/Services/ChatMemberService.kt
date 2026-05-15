package ChatMembers.Services

import ChatMembers.DTO.ChatMember
import ChatMembers.DTO.ChatMemberFilter
import ChatMembers.Repositories.ChatMemberRepository
import Encryption.Services.ChatEncryptionKeyService
import io.ktor.server.application.ApplicationEnvironment
import java.time.Instant

class ChatMemberService(environment: ApplicationEnvironment? = null) {
    private val repository = ChatMemberRepository()
    private val encryptionKeyService = ChatEncryptionKeyService(environment)

    suspend fun findById(id: Int): ChatMember? = repository.findById(id)

    suspend fun findByFilter(filter: ChatMemberFilter): List<ChatMember?> = repository.findByFilter(filter)

    suspend fun create(member: ChatMember): Int? {
        repository.create(member)
        val newId = repository.findByFilter(
            ChatMemberFilter(idChat = member.idChat, idUser = member.idUser)
        ).firstOrNull()?.id

        encryptionKeyService.rotateForUser(member.idChat, member.idUser)
        return newId
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

            val remainingMember = repository.findByFilter(
                ChatMemberFilter(idChat = member.idChat, isDeleted = false)
            ).filterNotNull().firstOrNull()

            if (remainingMember != null) {
                encryptionKeyService.rotateForUser(member.idChat, remainingMember.idUser)
            }
            return true
        }
        return false
    }
}
