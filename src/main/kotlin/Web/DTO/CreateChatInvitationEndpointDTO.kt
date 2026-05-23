package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatInvitationEndpointDTO(
    val idChat: Int,
    val inviteeUserId: Int,
    val message: String? = null
)
