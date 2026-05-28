package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordEndpointDTO(
    val currentPassword: String,
    val newPassword: String
)
