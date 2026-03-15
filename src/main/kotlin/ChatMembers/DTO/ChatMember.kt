package ChatMembers.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatMember(
    val id: Int,
    val idChat: Int,
    val idRole: Int,
    val idUser: Int,
    val createdAt: String,
    val deletedAt: String? = null
)