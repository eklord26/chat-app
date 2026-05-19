package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatInvitationEndpointDTO(
    val id: Int,
    val idChat: Int,
    val chat: ChatEndpointDTO? = null,
    val inviterUserId: Int,
    val inviteeUserId: Int,
    val inviter: UserEndpointDTO? = null,
    val invitee: UserEndpointDTO? = null,
    val idRole: Int,
    val status: String,
    val message: String? = null,
    val createdAt: String,
    val respondedAt: String? = null,
    val deletedAt: String? = null
)
