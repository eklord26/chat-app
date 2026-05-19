package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class DesignColorsEndpointDTO(
    val primary: String,
    val secondary: String,
    val warning: String,
    val error: String,
    val success: String
)
