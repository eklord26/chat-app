package Encryption.DAO

import Chats.DAO.ChatTable
import Encryption.DTO.ChatEncryptionKey
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object ChatEncryptionKeyTable : IntIdTable("chat_encryption_keys") {
    val idChat = reference("id_chat", ChatTable)
    val keyCipherText = text("key_cipher_text")
    val nonce = text("nonce")
    val algorithm = varchar("algorithm", 64)
    val version = integer("version")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val rotatedAt = timestamp("rotated_at").nullable()
    val revokedAt = timestamp("revoked_at").nullable()
}

class ChatEncryptionKeyDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ChatEncryptionKeyDAO>(ChatEncryptionKeyTable)

    var idChat by ChatEncryptionKeyTable.idChat
    var keyCipherText by ChatEncryptionKeyTable.keyCipherText
    var nonce by ChatEncryptionKeyTable.nonce
    var algorithm by ChatEncryptionKeyTable.algorithm
    var version by ChatEncryptionKeyTable.version
    var createdAt by ChatEncryptionKeyTable.createdAt
    var rotatedAt by ChatEncryptionKeyTable.rotatedAt
    var revokedAt by ChatEncryptionKeyTable.revokedAt
}

fun daoToModel(dao: ChatEncryptionKeyDAO?): ChatEncryptionKey? = dao?.let {
    ChatEncryptionKey(
        id = it.id.value,
        idChat = it.idChat.value,
        keyCipherText = it.keyCipherText,
        nonce = it.nonce,
        algorithm = it.algorithm,
        version = it.version,
        createdAt = it.createdAt.toString(),
        rotatedAt = it.rotatedAt?.toString(),
        revokedAt = it.revokedAt?.toString()
    )
}
