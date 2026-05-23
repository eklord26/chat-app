package Contacts.Services

import Contacts.DTO.Contact
import Contacts.DTO.ContactFilter
import Contacts.Repositories.ContactRepository
import java.time.Instant

class ContactService {
    private val repository = ContactRepository()

    suspend fun findById(id: Int): Contact? = repository.findById(id)

    suspend fun findByFilter(filter: ContactFilter): List<Contact?> = repository.findByFilter(filter)

    suspend fun create(contact: Contact): Int? {
        require(contact.ownerUserId != contact.contactUserId) { "Contact cannot point to the same user" }

        val existing = repository.findByFilter(
            ContactFilter(
                ownerUserId = contact.ownerUserId,
                contactUserId = contact.contactUserId,
                isDeleted = false
            )
        ).firstOrNull()

        if (existing != null) return existing.id

        repository.create(contact)
        return repository.findByFilter(
            ContactFilter(
                ownerUserId = contact.ownerUserId,
                contactUserId = contact.contactUserId,
                isDeleted = false
            )
        ).firstOrNull()?.id
    }

    suspend fun update(id: Int, contact: Contact): Boolean {
        require(contact.ownerUserId != contact.contactUserId) { "Contact cannot point to the same user" }

        if (repository.findById(id) != null) {
            repository.updateById(id, contact)
            return true
        }
        return false
    }

    suspend fun softDelete(id: Int): Boolean {
        val contact = repository.findById(id)
        if (contact != null) {
            repository.updateById(id, contact.copy(deletedAt = Instant.now().toString()))
            return true
        }
        return false
    }
}
