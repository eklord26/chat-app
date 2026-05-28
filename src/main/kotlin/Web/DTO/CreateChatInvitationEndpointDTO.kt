package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatInvitationEndpointDTO(
    val idChat: Int,
    val inviteeUserId: Int,
    val idRole: Int? = null,
    val message: String? = null
)
