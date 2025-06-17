package ChatMembers.DTO

data class ChatMember(
    val id: Int,
    val idChat: Int,
    val idRole: Int,
    val idUser: Int,
    val createdAt: String,
    val deletedAt: String,
    val deleted: Boolean
)
