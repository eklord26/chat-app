package Registration.DTO

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationDataDTO(
    val id: Int?,
    val status: String,
    val message: String,
    val authToken: String?,
    val encryptedKey: String?,
)
