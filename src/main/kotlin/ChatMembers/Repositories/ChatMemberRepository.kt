package ChatMembers.Repositories

import Base.Interfaces.IBaseRepository
import ChatMembers.DAO.ChatMemberDAO
import ChatMembers.DAO.daoToModel
import ChatMembers.DTO.ChatMember
import com.example.Base.Helpers.suspendTransaction
import java.time.Instant
import java.time.format.DateTimeFormatter

class ChatMemberRepository: IBaseRepository<ChatMember> {
    override suspend fun findById(id: Int): ChatMember? = suspendTransaction {
        daoToModel(ChatMemberDAO.findById(id))
    }

    override suspend fun findAll(): List<ChatMember?> = suspendTransaction {
        ChatMemberDAO.all().map(::daoToModel)
    }

    override suspend fun updateById(id: Int, entity: ChatMember): Unit = suspendTransaction {
        ChatMemberDAO.findByIdAndUpdate(
            id,
            {
                it: ChatMemberDAO->
                it.idChat = entity.idChat
                it.idRole = entity.idRole
                it.idUser = entity.idUser
                it.createdAt = Instant.from(
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss").parse(entity.createdAt)
                )
                it.deletedAt = Instant.from(
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss").parse(entity.deletedAt)
                )
                it.deleted = entity.deleted
            }
        )
    }

    override suspend fun create(entity: ChatMember): Unit = suspendTransaction {
        ChatMemberDAO.new {
            this.idChat = entity.idChat
            this.idUser = entity.idUser
            this.idRole = entity.idRole
            this.createdAt = Instant.now()
            this.deleted = entity.deleted
        }
    }
}