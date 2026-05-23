package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class UserEndpointDTO(
    val id: Int,
    val name: String,
    val login: String,
    val email: String? = null,
    val phone: String? = null,
    val fio: String? = null,
    val isAdmin: Boolean = false
)
