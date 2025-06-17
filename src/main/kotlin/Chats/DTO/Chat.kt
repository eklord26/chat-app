package Chats.DTO

data class Chat (
    val id: Int,
    val name: String,
    val owner: Int,
    val deleted: Boolean,
    val createdAt: String,
)
