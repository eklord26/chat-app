package Authentication.DTO

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationDataDTO(
    val id: Int? = null,
    val status: String,
    val message: String,
    val authToken: String?,
    val encryptedKey: String?,
)