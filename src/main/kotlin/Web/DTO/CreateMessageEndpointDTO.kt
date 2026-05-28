package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class CreateMessageEndpointDTO(
    val value: String = "",
    val type: String = "text",
    val mediaFileIds: List<Int> = emptyList()
)
