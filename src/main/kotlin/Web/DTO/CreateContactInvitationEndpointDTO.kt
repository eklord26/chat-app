package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class CreateContactInvitationEndpointDTO(
    val receiverUserId: Int,
    val message: String? = null
)
