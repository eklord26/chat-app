package Media.Repositories

import ChatMembers.DAO.ChatMemberDAO
import Media.DAO.MediaFileDAO
import Media.DAO.MediaFileTable
import Media.DAO.MessageAttachmentDAO
import Media.DAO.MessageAttachmentTable
import Media.DAO.daoToModel
import Media.DTO.MessageAttachment
import Messages.DAO.MessageDAO
import Messages.DAO.MessageTable
import com.example.Base.Helpers.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import java.time.Instant

class MessageAttachmentRepository {
    suspend fun create(idMessage: Int, idMediaFile: Int): MessageAttachment? = suspendTransaction {
        val dao = MessageAttachmentDAO.new {
            this.idMessage = EntityID(idMessage, MessageTable)
            this.idMediaFile = EntityID(idMediaFile, Media.DAO.MediaFileTable)
            createdAt = Instant.now()
        }
        daoToModel(dao)
    }

    suspend fun findByMessageIds(messageIds: Collection<Int>): List<Pair<MessageAttachment, Media.DTO.MediaFile>> =
        suspendTransaction {
            if (messageIds.isEmpty()) return@suspendTransaction emptyList()
            MessageAttachmentDAO
                .find { MessageAttachmentTable.idMessage inList messageIds.map { EntityID(it, MessageTable) } }
                .mapNotNull { attachmentDao ->
                    val attachment = daoToModel(attachmentDao) ?: return@mapNotNull null
                    val media = Media.DAO.daoToModel(MediaFileDAO.findById(attachment.idMediaFile)) ?: return@mapNotNull null
                    attachment to media
                }
        }

    suspend fun hasUserAccessToMedia(userId: Int, mediaFileId: Int): Boolean = suspendTransaction {
        MessageAttachmentDAO
            .find { MessageAttachmentTable.idMediaFile eq EntityID(mediaFileId, MediaFileTable) }
            .any { attachment ->
                val message = MessageDAO.findById(attachment.idMessage.value) ?: return@any false
                val senderMember = ChatMemberDAO.findById(message.idChatMember.value) ?: return@any false
                ChatMemberDAO
                    .find {
                        (ChatMembers.DAO.ChatMembersTable.idChat eq senderMember.idChat) and
                            (ChatMembers.DAO.ChatMembersTable.idUser eq EntityID(userId, com.example.Users.DAO.UserTable)) and
                            ChatMembers.DAO.ChatMembersTable.deletedAt.isNull()
                    }
                    .limit(1)
                    .any()
            }
    }
}
