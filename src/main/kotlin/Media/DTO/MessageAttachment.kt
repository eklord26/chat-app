package Media.DTO

import kotlinx.serialization.Serializable

@Serializable
data class MessageAttachment(
    val id: Int? = null,
    val idMessage: Int,
    val idMediaFile: Int,
    val createdAt: String
)
