package ChatMembers.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatMemberFilter (
    var idChat: Int? = null,
    var idUser: Int? = null,
    var idRole: Int? = null,
    var createdAt: String? = null,
    var deletedAt: String? = null,
)