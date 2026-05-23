package Invitations.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ContactInvitation(
    val id: Int? = null,
    val senderUserId: Int,
    val receiverUserId: Int,
    val status: String = "pending",
    val message: String? = null,
    val createdAt: String,
    val respondedAt: String? = null,
    val deletedAt: String? = null
)
