package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatEndpointDTO(
    val id: Int,
    val name: String,
    val owner: Int,
    val ownerUser: UserEndpointDTO? = null,
    val currentUserMemberId: Int? = null,
    val createdAt: String,
    val deletedAt: String? = null
)
