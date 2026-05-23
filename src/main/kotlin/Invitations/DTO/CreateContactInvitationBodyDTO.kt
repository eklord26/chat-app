package Invitations.DTO

import kotlinx.serialization.Serializable

@Serializable
data class CreateContactInvitationBodyDTO(
    val receiverUserId: Int,
    val message: String? = null
)
