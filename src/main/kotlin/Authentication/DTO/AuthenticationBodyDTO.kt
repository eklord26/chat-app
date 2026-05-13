package Authentication.DTO

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationBodyDTO(
    val password: String,
    val login: String
)
