package Chats.DAO

import Chats.DTO.Chat
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object ChatTable : IntIdTable("chats")
{
    val owner = integer("owner")
    val name = text("name")
    val createdAt = timestamp("created_at")
    val deleted = bool("deleted")
}

class ChatDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ChatDAO>(ChatTable)

    var owner by ChatTable.owner
    var name by ChatTable.name
    var createdAt by ChatTable.createdAt
    var deleted by ChatTable.deleted
}

fun daoToModel(dao: ChatDAO?): Chat? = dao?.let {
    Chat (
        it.id.value,
        it.name,
        it.owner,
        it.deleted,
        it.createdAt.toString()
    )
}