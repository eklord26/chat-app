package Base.DTO

// Класс для ответа на запрос
data class ResponseDTO(
    val id: Int?,
    val status: String,
    val message: String
)
