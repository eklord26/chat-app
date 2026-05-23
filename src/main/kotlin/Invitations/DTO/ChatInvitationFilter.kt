package Invitations.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ChatInvitationFilter(
    val idChat: Int? = null,
    val inviterUserId: Int? = null,
    val inviteeUserId: Int? = null,
    val status: String? = null,
    val isDeleted: Boolean? = null
)
