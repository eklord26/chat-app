package Messages.Repositories

import Base.Interfaces.IBaseRepository
import Messages.DAO.MessageDAO
import Messages.DAO.MessageTable
import Messages.DAO.daoToModel
import Messages.DTO.Message
import Messages.DTO.MessageFilter
import com.example.Base.Helpers.suspendTransaction
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import java.time.Instant

class MessageRepository : IBaseRepository<Message, MessageFilter> {

    override suspend fun findById(id: Int): Message? = suspendTransaction {
        daoToModel(MessageDAO.findById(id))
    }

    override suspend fun findAll(): List<Message?> = suspendTransaction {
        MessageDAO.all().map(::daoToModel)
    }

    override suspend fun findByFilter(filter: MessageFilter): List<Message?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.idChatMember?.let { conditions.add(MessageTable.idChatMember eq it) }
        filter.value?.let { conditions.add(MessageTable.value like "%$it%") }
        filter.type?.let { conditions.add(MessageTable.type eq it.string) }

        filter.isDeleted?.let { isDeleted ->
            if (isDeleted) conditions.add(MessageTable.deletedAt.isNotNull())
            else conditions.add(MessageTable.deletedAt.isNull())
        }

        filter.createdAt?.let { conditions.add(MessageTable.createdAt eq Instant.parse(it)) }
        filter.viewedAt?.let { conditions.add(MessageTable.viewedAt eq Instant.parse(it)) }

        if (conditions.isEmpty()) {
            MessageDAO.all().map(::daoToModel)
        } else {
            val finalOp = conditions.reduce { acc, op -> acc and op }
            MessageDAO.find(finalOp).map(::daoToModel)
        }
    }

    override suspend fun updateById(id: Int, entity: Message): Unit = suspendTransaction {
        MessageDAO.findByIdAndUpdate(id) {
            it.idChatMember = entity.idChatMember
            it.value = entity.value
            it.type = entity.type?.string ?: "text"
            it.viewedAt = entity.viewedAt?.let { date -> Instant.parse(date) }
            it.deletedAt = entity.deletedAt?.let { date -> Instant.parse(date) }
        }
    }

    override suspend fun create(entity: Message): Unit = suspendTransaction {
        MessageDAO.new {
            idChatMember = entity.idChatMember
            value = entity.value
            type = entity.type?.string ?: "text"
            createdAt = Instant.now()
            viewedAt = null
            deletedAt = null
        }
    }
}