package Messages.Repositories

import Base.Interfaces.IBaseRepository
import com.example.Base.Helpers.suspendTransaction
import Messages.DAO.MessageDAO
import Messages.DAO.MessageTable
import Messages.DAO.daoToModel
import Messages.DTO.Message
import Messages.Enum.MessageTypeEnum
import Messages.Interfaces.IMessageRepository
import java.time.Instant
import java.time.format.DateTimeFormatter

class MessageRepository: IBaseRepository<Message>, IMessageRepository {
    override suspend fun findByValue(value: String): List<Message?> = suspendTransaction {
        MessageDAO
            .find { (MessageTable.value eq value) }
            .map(::daoToModel)
    }

    override suspend fun findByType(type: MessageTypeEnum): List<Message?> = suspendTransaction {
        MessageDAO
            .find { (MessageTable.type eq type.string) }
            .map(::daoToModel)
    }

    override suspend fun findByIdChatMember(idChatMember: Int): List<Message?> = suspendTransaction {
        MessageDAO
            .find { (MessageTable.idChatMember eq idChatMember) }
            .map(::daoToModel)
    }

    override suspend fun findById(id: Int): Message? = suspendTransaction {
        daoToModel(MessageDAO.findById(id))
    }

    override suspend fun findAll(): List<Message?> = suspendTransaction {
        MessageDAO.all().map(::daoToModel)
    }

    override suspend fun updateById(id: Int, entity: Message): Unit = suspendTransaction {
        MessageDAO.findByIdAndUpdate(
            id,
            { it: MessageDAO->
                it.idChatMember = entity.idChatMember
                it.value = entity.value
                it.type = entity.type!!.string
                it.createdAt = Instant.from(
                    DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss").parse(entity.createdAt)
                )
                it.viewedAt = Instant.from(
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss").parse(entity.viewedAt)
                )
                it.deletedAt = Instant.from(
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss").parse(entity.deletedAt)
                )
                it.deleted = entity.deleted
            }
        )
    }

    override suspend fun create(entity: Message): Unit = suspendTransaction {
        MessageDAO.new {
            idChatMember = entity.idChatMember
            value = entity.value
            type = entity.type!!.string
            createdAt = Instant.now()
            deleted = false
        }
    }
}