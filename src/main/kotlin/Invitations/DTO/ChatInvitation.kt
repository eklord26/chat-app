package Invitations.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatInvitation(
    val id: Int? = null,
    val idChat: Int,
    val inviterUserId: Int,
    val inviteeUserId: Int,
    val idRole: Int = 0,
    val status: String = "pending",
    val message: String? = null,
    val createdAt: String,
    val respondedAt: String? = null,
    val deletedAt: String? = null
)
