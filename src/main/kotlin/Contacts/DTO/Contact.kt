package Contacts.DTO

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: Int? = null,
    val ownerUserId: Int,
    val contactUserId: Int,
    val displayName: String? = null,
    val createdAt: String,
    val deletedAt: String? = null
)
