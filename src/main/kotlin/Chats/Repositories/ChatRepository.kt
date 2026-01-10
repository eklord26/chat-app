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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import java.time.Instant
import java.time.format.DateTimeFormatter

class ChatRepository: IBaseRepository<Chat, ChatFilter> {
    override suspend fun findById(id: Int): Chat? = suspendTransaction {
        daoToModel(ChatDAO.findById(id))
    }

    override suspend fun findAll(): List<Chat?> = suspendTransaction {
        ChatDAO.all().map(::daoToModel)
    }

    override suspend fun findByFilter(filter: ChatFilter): List<Chat?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.owner?.let {
            conditions.add(ChatTable.owner eq it)
        }

        filter.name?.let {
            conditions.add(ChatTable.name like "%$it%")
        }

        filter.createdAt?.let {
            conditions.add(ChatTable.createdAt eq Instant.from(
                DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss").parse(it)
            ))
        }

        filter.deleted?.let {
            conditions.add(ChatTable.deleted eq it)
        }

        if (conditions.isEmpty()) {
            ChatDAO.all().map(::daoToModel)
        } else {
            val finalOp = conditions.reduce { acc, op -> acc and op }
            ChatDAO.find(finalOp).map(::daoToModel)
        }
    }

    override suspend fun updateById(id: Int, entity: Chat): Unit = suspendTransaction {
        ChatDAO.findByIdAndUpdate(
            id,
            {
                it: ChatDAO->
                it.owner = entity.owner
                it.name = entity.name
                it.deleted = entity.deleted
                it.createdAt = Instant.from(
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss").parse(entity.createdAt)
                )
            }
        )
    }

    override suspend fun create(entity: Chat): Unit = suspendTransaction {
        ChatDAO.new {
            owner = entity.owner
            name = entity.name
            deleted = entity.deleted
            createdAt = Instant.now()
        }
    }
}