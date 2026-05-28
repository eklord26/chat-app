package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatEndpointDTO(
    val id: Int,
    val name: String,
    val owner: Int,
    val ownerUser: UserEndpointDTO? = null,
    val currentUserMemberId: Int? = null,
    val participants: List<UserEndpointDTO> = emptyList(),
    val unreadCount: Int = 0,
    val createdAt: String,
    val deletedAt: String? = null
)
