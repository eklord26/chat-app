package Chats.DTO

import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: Int? = null,
    val name: String,
    val owner: Int,
    val createdAt: String,
    val deletedAt: String? = null
)