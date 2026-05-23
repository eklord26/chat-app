package Invitations.DTO

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatInvitationBodyDTO(
    val idChat: Int,
    val inviteeUserId: Int,
    val message: String? = null
)
