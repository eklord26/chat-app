package Contacts.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ContactFilter(
    val ownerUserId: Int? = null,
    val contactUserId: Int? = null,
    val isDeleted: Boolean? = null
)
