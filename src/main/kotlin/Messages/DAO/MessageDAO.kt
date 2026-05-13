package Messages.DAO

import ChatMembers.DAO.ChatMembersTable
import Messages.DTO.Message
import Messages.Enum.MessageTypeEnum.Companion.getEnumByType
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object MessageTable : IntIdTable("messages") {
    val idChatMember = reference("id_chat_member", ChatMembersTable)
    val value = text("value")
    val type = text("type")
    val isEncrypted = bool("is_encrypted").default(false)
    val encryptionAlgorithm = varchar("encryption_algorithm", 64).nullable()
    val encryptionKeyVersion = integer("encryption_key_version").nullable()
    val encryptionNonce = text("encryption_nonce").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val viewedAt = timestamp("viewed_at").nullable()
    val deletedAt = timestamp("deleted_at").nullable()
}

class MessageDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MessageDAO>(MessageTable)

    var idChatMember by MessageTable.idChatMember
    var value by MessageTable.value
    var type by MessageTable.type
    var isEncrypted by MessageTable.isEncrypted
    var encryptionAlgorithm by MessageTable.encryptionAlgorithm
    var encryptionKeyVersion by MessageTable.encryptionKeyVersion
    var encryptionNonce by MessageTable.encryptionNonce
    var createdAt by MessageTable.createdAt
    var viewedAt by MessageTable.viewedAt
    var deletedAt by MessageTable.deletedAt
}

fun daoToModel(dao: MessageDAO?): Message? = dao?.let {
    Message(
        it.id.value,
        it.idChatMember.value,
        it.value,
        getEnumByType(it.type),
        it.isEncrypted,
        it.encryptionAlgorithm,
        it.encryptionKeyVersion,
        it.encryptionNonce,
        it.createdAt.toString(),
        it.viewedAt?.toString(),
        it.deletedAt?.toString()
    )
}
