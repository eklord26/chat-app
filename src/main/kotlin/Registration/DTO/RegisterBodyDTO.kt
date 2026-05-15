package Registration.DTO

import kotlinx.serialization.Serializable

@Serializable
data class RegisterBodyDTO(
    val username: String,
    val email: String,
    val phone: String,
    val fio: String,
    val password: String,
    val login: String
)
