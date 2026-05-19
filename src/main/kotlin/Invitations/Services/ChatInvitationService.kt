package Invitations.Services

import ChatMembers.DTO.ChatMember
import ChatMembers.Services.ChatMemberService
import Invitations.DTO.ChatInvitation
import Invitations.DTO.ChatInvitationFilter
import Invitations.Enums.InvitationStatusEnum
import Invitations.Repositories.ChatInvitationRepository
import java.time.Instant

class ChatInvitationService {
    private val repository = ChatInvitationRepository()
    private val chatMemberService = ChatMemberService()

    suspend fun findById(id: Int): ChatInvitation? = repository.findById(id)

    suspend fun findByFilter(filter: ChatInvitationFilter): List<ChatInvitation?> = repository.findByFilter(filter)

    suspend fun create(invitation: ChatInvitation): Int? {
        require(invitation.inviterUserId != invitation.inviteeUserId) { "Cannot invite yourself to chat" }

        repository.create(invitation.copy(status = InvitationStatusEnum.PENDING.value))
        return repository.findByFilter(
            ChatInvitationFilter(
                idChat = invitation.idChat,
                inviteeUserId = invitation.inviteeUserId,
                status = InvitationStatusEnum.PENDING.value,
                isDeleted = false
            )
        ).firstOrNull()?.id
    }

    suspend fun update(id: Int, invitation: ChatInvitation): Boolean {
        if (repository.findById(id) != null) {
            repository.updateById(id, invitation)
            return true
        }
        return false
    }

    suspend fun accept(id: Int, currentUserId: Int): Boolean {
        val invitation = repository.findById(id) ?: return false
        if (invitation.inviteeUserId != currentUserId || invitation.status != InvitationStatusEnum.PENDING.value) {
            return false
        }

        val now = Instant.now().toString()
        chatMemberService.create(
            ChatMember(
                idChat = invitation.idChat,
                idRole = invitation.idRole,
                idUser = invitation.inviteeUserId,
                createdAt = now
            )
        )

        repository.updateById(
            id,
            invitation.copy(
                status = InvitationStatusEnum.ACCEPTED.value,
                respondedAt = now
            )
        )
        return true
    }

    suspend fun reject(id: Int, currentUserId: Int): Boolean {
        val invitation = repository.findById(id) ?: return false
        if (invitation.inviteeUserId != currentUserId || invitation.status != InvitationStatusEnum.PENDING.value) {
            return false
        }

        repository.updateById(
            id,
            invitation.copy(
                status = InvitationStatusEnum.REJECTED.value,
                respondedAt = Instant.now().toString()
            )
        )
        return true
    }

    suspend fun cancel(id: Int, currentUserId: Int): Boolean {
        val invitation = repository.findById(id) ?: return false
        if (invitation.inviterUserId != currentUserId || invitation.status != InvitationStatusEnum.PENDING.value) {
            return false
        }

        repository.updateById(
            id,
            invitation.copy(
                status = InvitationStatusEnum.CANCELLED.value,
                respondedAt = Instant.now().toString()
            )
        )
        return true
    }

    suspend fun softDelete(id: Int): Boolean {
        val invitation = repository.findById(id)
        if (invitation != null) {
            repository.updateById(id, invitation.copy(deletedAt = Instant.now().toString()))
            return true
        }
        return false
    }
}
