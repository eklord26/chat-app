package ChatMembers.Repositories

import Base.Interfaces.IBaseRepository
import ChatMembers.DAO.ChatMemberDAO
import ChatMembers.DAO.ChatMembersTable
import ChatMembers.DAO.daoToModel
import ChatMembers.DTO.ChatMember
import ChatMembers.DTO.ChatMemberFilter
import com.example.Base.Helpers.suspendTransaction
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import java.time.Instant

class ChatMemberRepository : IBaseRepository<ChatMember, ChatMemberFilter> {
    override suspend fun findById(id: Int): ChatMember? = suspendTransaction {
        daoToModel(ChatMemberDAO.findById(id))
    }

    override suspend fun findAll(): List<ChatMember?> = suspendTransaction {
        ChatMemberDAO.all().map(::daoToModel)
    }

    override suspend fun findByFilter(filter: ChatMemberFilter): List<ChatMember?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.idChat?.let { conditions.add(ChatMembersTable.idChat eq it) }
        filter.idRole?.let { conditions.add(ChatMembersTable.idRole eq it) }
        filter.idUser?.let { conditions.add(ChatMembersTable.idUser eq it) }

        filter.isDeleted?.let { isDeleted ->
            if (isDeleted) conditions.add(ChatMembersTable.deletedAt.isNotNull())
            else conditions.add(ChatMembersTable.deletedAt.isNull())
        }

        if (conditions.isEmpty()) {
            ChatMemberDAO.all().map(::daoToModel)
        } else {
            val finalOp = conditions.reduce { acc, op -> acc and op }
            ChatMemberDAO.find(finalOp).map(::daoToModel)
        }
    }

    override suspend fun updateById(id: Int, entity: ChatMember): Unit = suspendTransaction {
        ChatMemberDAO.findByIdAndUpdate(id) {
            it.idChat = entity.idChat
            it.idRole = entity.idRole
            it.idUser = entity.idUser
            it.deletedAt = entity.deletedAt?.let { date -> Instant.parse(date) }
        }
    }

    override suspend fun create(entity: ChatMember): Unit = suspendTransaction {
        ChatMemberDAO.new {
            idChat = entity.idChat
            idRole = entity.idRole
            idUser = entity.idUser
            createdAt = Instant.now()
            deletedAt = null
        }
    }
}