package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatEndpointDTO(
    val name: String,
    val idRole: Int = 1
)
