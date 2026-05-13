package Encryption.Repositories

import Chats.DAO.ChatTable
import Encryption.DAO.ChatEncryptionKeyDAO
import Encryption.DAO.ChatEncryptionKeyTable
import Encryption.DAO.daoToModel
import Encryption.DTO.ChatEncryptionKey
import com.example.Base.Helpers.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import java.time.Instant

class ChatEncryptionKeyRepository {
    suspend fun findActiveByChat(idChat: Int): ChatEncryptionKey? = suspendTransaction {
        ChatEncryptionKeyDAO
            .find((ChatEncryptionKeyTable.idChat eq EntityID(idChat, ChatTable)) and ChatEncryptionKeyTable.revokedAt.isNull())
            .orderBy(ChatEncryptionKeyTable.version to SortOrder.DESC)
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    suspend fun findByChatAndVersion(idChat: Int, version: Int): ChatEncryptionKey? = suspendTransaction {
        ChatEncryptionKeyDAO
            .find((ChatEncryptionKeyTable.idChat eq EntityID(idChat, ChatTable)) and (ChatEncryptionKeyTable.version eq version))
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    suspend fun findAllByChat(idChat: Int): List<ChatEncryptionKey> = suspendTransaction {
        ChatEncryptionKeyDAO
            .find(ChatEncryptionKeyTable.idChat eq EntityID(idChat, ChatTable))
            .orderBy(ChatEncryptionKeyTable.version to SortOrder.ASC)
            .mapNotNull(::daoToModel)
    }

    suspend fun create(entity: ChatEncryptionKey): ChatEncryptionKey? = suspendTransaction {
        val dao = ChatEncryptionKeyDAO.new {
            idChat = EntityID(entity.idChat, ChatTable)
            keyCipherText = entity.keyCipherText
            nonce = entity.nonce
            algorithm = entity.algorithm
            version = entity.version
            createdAt = Instant.now()
            rotatedAt = entity.rotatedAt?.let { date -> Instant.parse(date) }
            revokedAt = null
        }
        daoToModel(dao)
    }

    suspend fun revokeActive(idChat: Int) = suspendTransaction {
        ChatEncryptionKeyDAO
            .find((ChatEncryptionKeyTable.idChat eq EntityID(idChat, ChatTable)) and ChatEncryptionKeyTable.revokedAt.isNull())
            .forEach { it.revokedAt = Instant.now() }
    }
}
