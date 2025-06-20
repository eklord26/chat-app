package Base.DTO

import kotlinx.serialization.Serializable

// Класс для ответа на запрос
@Serializable
data class ResponseDTO(
    val id: Int?,
    val status: String,
    val message: String,
)
