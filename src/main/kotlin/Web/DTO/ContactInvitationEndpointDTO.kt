package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ContactInvitationEndpointDTO(
    val id: Int,
    val senderUserId: Int,
    val receiverUserId: Int,
    val sender: UserEndpointDTO? = null,
    val receiver: UserEndpointDTO? = null,
    val status: String,
    val message: String? = null,
    val createdAt: String,
    val respondedAt: String? = null,
    val deletedAt: String? = null
)
