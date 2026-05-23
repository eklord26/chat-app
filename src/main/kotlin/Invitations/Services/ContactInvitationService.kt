package Invitations.Services

import Contacts.DTO.Contact
import Contacts.Services.ContactService
import Invitations.DTO.ContactInvitation
import Invitations.DTO.ContactInvitationFilter
import Invitations.Enums.InvitationStatusEnum
import Invitations.Repositories.ContactInvitationRepository
import java.time.Instant

class ContactInvitationService {
    private val repository = ContactInvitationRepository()
    private val contactService = ContactService()

    suspend fun findById(id: Int): ContactInvitation? = repository.findById(id)

    suspend fun findByFilter(filter: ContactInvitationFilter): List<ContactInvitation?> = repository.findByFilter(filter)

    suspend fun create(invitation: ContactInvitation): Int? {
        require(invitation.senderUserId != invitation.receiverUserId) { "Cannot invite yourself to contacts" }

        repository.create(invitation.copy(status = InvitationStatusEnum.PENDING.value))
        return repository.findByFilter(
            ContactInvitationFilter(
                senderUserId = invitation.senderUserId,
                receiverUserId = invitation.receiverUserId,
                status = InvitationStatusEnum.PENDING.value,
                isDeleted = false
            )
        ).firstOrNull()?.id
    }

    suspend fun update(id: Int, invitation: ContactInvitation): Boolean {
        if (repository.findById(id) != null) {
            repository.updateById(id, invitation)
            return true
        }
        return false
    }

    suspend fun accept(id: Int, currentUserId: Int): Boolean {
        val invitation = repository.findById(id) ?: return false
        if (invitation.receiverUserId != currentUserId || invitation.status != InvitationStatusEnum.PENDING.value) {
            return false
        }

        val now = Instant.now().toString()
        contactService.create(
            Contact(
                ownerUserId = invitation.senderUserId,
                contactUserId = invitation.receiverUserId,
                createdAt = now
            )
        )
        contactService.create(
            Contact(
                ownerUserId = invitation.receiverUserId,
                contactUserId = invitation.senderUserId,
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
        if (invitation.receiverUserId != currentUserId || invitation.status != InvitationStatusEnum.PENDING.value) {
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
        if (invitation.senderUserId != currentUserId || invitation.status != InvitationStatusEnum.PENDING.value) {
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
