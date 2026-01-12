package Messages.Repositories

import Base.Interfaces.IBaseRepository
import com.example.Base.Helpers.suspendTransaction
import Messages.DAO.MessageDAO
import Messages.DAO.MessageTable
import Messages.DAO.daoToModel
import Messages.DTO.Message
import Messages.DTO.MessageFilter
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import java.time.Instant
import java.time.format.DateTimeFormatter

class MessageRepository: IBaseRepository<Message, MessageFilter> {

    override suspend fun findById(id: Int): Message? = suspendTransaction {
        daoToModel(MessageDAO.findById(id))
    }

    override suspend fun findAll(): List<Message?> = suspendTransaction {
        MessageDAO.all().map(::daoToModel)
    }

    override suspend fun findByFilter(filter: MessageFilter): List<Message?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.idChatMember?.let {
            conditions.add(MessageTable.idChatMember eq it)
        }

        filter.value?.let {
            conditions.add(MessageTable.value like "%$it%")
        }

        filter.createdAt?.let {
            conditions.add(
                MessageTable.createdAt eq Instant.from(
                DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss").parse(it)
                )
            )
        }

        filter.deletedAt?.let {
            conditions.add(
                MessageTable.deletedAt eq Instant.from(
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss").parse(it)
                )
            )
        }

        filter.viewedAt?.let {
            conditions.add(
                MessageTable.viewedAt eq Instant.from(
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss").parse(it)
                )
            )
        }

        filter.deleted?.let {
            conditions.add(MessageTable.deleted eq it)
        }

        filter.type?.let {
            conditions.add(MessageTable.type eq it.string)
        }

        if (conditions.isEmpty()) {
            MessageDAO.all().map(::daoToModel)
        } else {
            val finalOp = conditions.reduce { acc, op -> acc and op }
            MessageDAO.find(finalOp).map(::daoToModel)
        }
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