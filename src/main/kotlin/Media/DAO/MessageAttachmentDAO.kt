package Media.DAO

import Media.DTO.MessageAttachment
import Messages.DAO.MessageTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object MessageAttachmentTable : IntIdTable("message_attachments") {
    val idMessage = reference("id_message", MessageTable)
    val idMediaFile = reference("id_media_file", MediaFileTable)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

class MessageAttachmentDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MessageAttachmentDAO>(MessageAttachmentTable)

    var idMessage by MessageAttachmentTable.idMessage
    var idMediaFile by MessageAttachmentTable.idMediaFile
    var createdAt by MessageAttachmentTable.createdAt
}

fun daoToModel(dao: MessageAttachmentDAO?): MessageAttachment? = dao?.let {
    MessageAttachment(
        id = it.id.value,
        idMessage = it.idMessage.value,
        idMediaFile = it.idMediaFile.value,
        createdAt = it.createdAt.toString()
    )
}
