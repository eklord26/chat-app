package Chats.DAO

import Chats.DTO.Chat
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ChatTable : IntIdTable("chats") {
    val owner = integer("owner")
    val name = text("name")
    val createdAt = timestamp("created_at")
    val deletedAt = timestamp("deleted_at").nullable()
}

class ChatDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ChatDAO>(ChatTable)

    var owner by ChatTable.owner
    var name by ChatTable.name
    var createdAt by ChatTable.createdAt
    var deletedAt by ChatTable.deletedAt
}

private val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

fun daoToModel(dao: ChatDAO?): Chat? = dao?.let {
    Chat(
        it.id.value,
        it.name,
        it.owner,
        formatter.format(it.createdAt),
        it.deletedAt?.let { date -> formatter.format(date) }
    )
}