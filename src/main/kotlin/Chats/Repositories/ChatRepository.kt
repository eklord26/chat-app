package Chats.Repositories

import Base.Interfaces.IBaseRepository
import Chats.DAO.ChatDAO
import Chats.DAO.ChatTable
import Chats.DAO.daoToModel
import Chats.DTO.Chat
import Chats.DTO.ChatFilter
import com.example.Base.Helpers.suspendTransaction
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import java.time.Instant

class ChatRepository : IBaseRepository<Chat, ChatFilter> {
    override suspend fun findById(id: Int): Chat? = suspendTransaction {
        daoToModel(ChatDAO.findById(id))
    }

    override suspend fun findAll(): List<Chat?> = suspendTransaction {
        ChatDAO.all().map(::daoToModel)
    }

    override suspend fun findByFilter(filter: ChatFilter): List<Chat?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.owner?.let { conditions.add(ChatTable.owner eq it) }
        filter.name?.let { conditions.add(ChatTable.name like "%$it%") }

        filter.isDeleted?.let { isDeleted ->
            if (isDeleted) conditions.add(ChatTable.deletedAt.isNotNull())
            else conditions.add(ChatTable.deletedAt.isNull())
        }

        filter.createdAt?.let {
            conditions.add(ChatTable.createdAt eq Instant.parse(it))
        }

        if (conditions.isEmpty()) {
            ChatDAO.all().map(::daoToModel)
        } else {
            val finalOp = conditions.reduce { acc, op -> acc and op }
            ChatDAO.find(finalOp).map(::daoToModel)
        }
    }

    override suspend fun updateById(id: Int, entity: Chat): Unit = suspendTransaction {
        ChatDAO.findByIdAndUpdate(id) {
            it.owner = entity.owner
            it.name = entity.name
            it.deletedAt = entity.deletedAt?.let { date -> Instant.parse(date) }
        }
    }

    override suspend fun create(entity: Chat): Unit = suspendTransaction {
        ChatDAO.new {
            owner = entity.owner
            name = entity.name
            createdAt = Instant.now()
            deletedAt = null
        }
    }
}