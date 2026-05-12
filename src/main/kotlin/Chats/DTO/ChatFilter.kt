package Chats.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatFilter(
    val owner: Int? = null,
    val isDeleted: Boolean? = null,
    val createdAt: String? = null,
    val name: String? = null
)