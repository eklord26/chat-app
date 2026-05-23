package Contacts.DTO

import kotlinx.serialization.Serializable

@Serializable
data class CreateContactBodyDTO(
    val contactUserId: Int,
    val displayName: String? = null
)
