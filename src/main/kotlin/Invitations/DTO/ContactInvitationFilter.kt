package Invitations.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ContactInvitationFilter(
    val senderUserId: Int? = null,
    val receiverUserId: Int? = null,
    val status: String? = null,
    val isDeleted: Boolean? = null
)
