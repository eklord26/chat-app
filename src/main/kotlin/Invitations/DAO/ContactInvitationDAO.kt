package Invitations.DAO

import Invitations.DTO.ContactInvitation
import com.example.Users.DAO.UserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ContactInvitationTable : IntIdTable("contact_invitations") {
    val senderUserId = reference("sender_user_id", UserTable)
    val receiverUserId = reference("receiver_user_id", UserTable)
    val status = text("status").default("pending")
    val message = text("message").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val respondedAt = timestamp("responded_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}

class ContactInvitationDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ContactInvitationDAO>(ContactInvitationTable)

    var senderUserId by ContactInvitationTable.senderUserId
    var receiverUserId by ContactInvitationTable.receiverUserId
    var status by ContactInvitationTable.status
    var message by ContactInvitationTable.message
    var createdAt by ContactInvitationTable.createdAt
    var respondedAt by ContactInvitationTable.respondedAt
    var deletedAt by ContactInvitationTable.deletedAt
}

private val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

fun daoToModel(dao: ContactInvitationDAO?): ContactInvitation? = dao?.let {
    ContactInvitation(
        it.id.value,
        it.senderUserId.value,
        it.receiverUserId.value,
        it.status,
        it.message,
        formatter.format(it.createdAt),
        it.respondedAt?.let { date -> formatter.format(date) },
        it.deletedAt?.let { date -> formatter.format(date) }
    )
}
