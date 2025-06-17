package Chats.Repositories

import Base.Interfaces.IBaseRepository
import Chats.DAO.ChatDAO
import Chats.DAO.daoToModel
import Chats.DTO.Chat
import com.example.Base.Helpers.suspendTransaction
import java.time.Instant
import java.time.format.DateTimeFormatter

class ChatRepository: IBaseRepository<Chat> {
    override suspend fun findById(id: Int): Chat? = suspendTransaction {
        daoToModel(ChatDAO.findById(id))
    }

    override suspend fun findAll(): List<Chat?> = suspendTransaction {
        ChatDAO.all().map(::daoToModel)
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