package Messages.DAO

import Messages.DTO.Message
import Messages.Enum.MessageTypeEnum.Companion.getEnumByType
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object MessageTable : IntIdTable("messages")
{
    val idChatMember = integer("id_chat_member")
    val value = text("value")
    val type = text("type")
    val createdAt = timestamp("created_at")
    val viewedAt = timestamp("viewed_at")
    val deletedAt = timestamp("deleted_at")
    val deleted = bool("deleted")
}

class MessageDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MessageDAO>(MessageTable)

    var idChatMember by MessageTable.idChatMember
    var value by MessageTable.value
    var type by MessageTable.type
    var createdAt by MessageTable.createdAt
    var viewedAt by MessageTable.viewedAt
    var deletedAt by MessageTable.deletedAt
    var deleted by MessageTable.deleted
}

fun daoToModel(dao: MessageDAO?): Message? = dao?.let {
    Message (
        it.id.value,
        it.idChatMember,
        it.value,
        getEnumByType(it.type),
        it.createdAt.toString(),
        it.viewedAt.toString(),
        it.deletedAt.toString(),
        it.deleted
    )
}