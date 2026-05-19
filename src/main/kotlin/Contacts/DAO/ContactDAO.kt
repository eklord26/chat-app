package Contacts.DAO

import Contacts.DTO.Contact
import com.example.Users.DAO.UserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ContactTable : IntIdTable("contacts") {
    val ownerUserId = reference("owner_user_id", UserTable)
    val contactUserId = reference("contact_user_id", UserTable)
    val displayName = text("display_name").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val deletedAt = timestamp("deleted_at").nullable()
}

class ContactDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ContactDAO>(ContactTable)

    var ownerUserId by ContactTable.ownerUserId
    var contactUserId by ContactTable.contactUserId
    var displayName by ContactTable.displayName
    var createdAt by ContactTable.createdAt
    var deletedAt by ContactTable.deletedAt
}

private val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

fun daoToModel(dao: ContactDAO?): Contact? = dao?.let {
    Contact(
        it.id.value,
        it.ownerUserId.value,
        it.contactUserId.value,
        it.displayName,
        formatter.format(it.createdAt),
        it.deletedAt?.let { date -> formatter.format(date) }
    )
}
