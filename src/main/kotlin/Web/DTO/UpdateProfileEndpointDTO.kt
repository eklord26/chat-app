package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileEndpointDTO(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val fio: String? = null
)
