package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatRoleEndpointDTO(
    val id: Int,
    val name: String
)
