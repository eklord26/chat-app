package Invitations.DAO

import Chats.DAO.ChatTable
import Invitations.DTO.ChatInvitation
import Roles.DAO.RoleTable
import com.example.Users.DAO.UserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ChatInvitationTable : IntIdTable("chat_invitations") {
    val idChat = reference("id_chat", ChatTable)
    val inviterUserId = reference("inviter_user_id", UserTable)
    val inviteeUserId = reference("invitee_user_id", UserTable)
    val idRole = reference("id_role", RoleTable)
    val status = text("status").default("pending")
    val message = text("message").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val respondedAt = timestamp("responded_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}

class ChatInvitationDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ChatInvitationDAO>(ChatInvitationTable)

    var idChat by ChatInvitationTable.idChat
    var inviterUserId by ChatInvitationTable.inviterUserId
    var inviteeUserId by ChatInvitationTable.inviteeUserId
    var idRole by ChatInvitationTable.idRole
    var status by ChatInvitationTable.status
    var message by ChatInvitationTable.message
    var createdAt by ChatInvitationTable.createdAt
    var respondedAt by ChatInvitationTable.respondedAt
    var deletedAt by ChatInvitationTable.deletedAt
}

private val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

fun daoToModel(dao: ChatInvitationDAO?): ChatInvitation? = dao?.let {
    ChatInvitation(
        it.id.value,
        it.idChat.value,
        it.inviterUserId.value,
        it.inviteeUserId.value,
        it.idRole.value,
        it.status,
        it.message,
        formatter.format(it.createdAt),
        it.respondedAt?.let { date -> formatter.format(date) },
        it.deletedAt?.let { date -> formatter.format(date) }
    )
}
