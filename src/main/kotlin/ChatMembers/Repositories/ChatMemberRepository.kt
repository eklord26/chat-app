package ChatMembers.Repositories

import Base.Interfaces.IBaseRepository
import com.example.Base.Helpers.suspendTransaction

import ChatMembers.DAO.ChatMemberDAO
import ChatMembers.DAO.ChatMembersTable
import ChatMembers.DAO.daoToModel
import ChatMembers.DTO.ChatMember
import ChatMembers.DTO.ChatMemberFilter

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

import java.time.Instant
import java.time.format.DateTimeFormatter

class ChatMemberRepository: IBaseRepository<ChatMember, ChatMemberFilter> {
    override suspend fun findById(id: Int): ChatMember? = suspendTransaction {
        daoToModel(ChatMemberDAO.findById(id))
    }

    override suspend fun findAll(): List<ChatMember?> = suspendTransaction {
        ChatMemberDAO.all().map(::daoToModel)
    }

    override suspend fun findByFilter(filter: ChatMemberFilter): List<ChatMember?> = suspendTransaction {
        // 1. Создаем список условий
        val conditions = mutableListOf<Op<Boolean>>()

        filter.idChat?.let {
            conditions.add(ChatMembersTable.idChat eq it)
        }

        filter.idRole?.let {
            conditions.add(ChatMembersTable.idRole eq it)
        }

        filter.idUser?.let {
            conditions.add(ChatMembersTable.idUser eq it)
        }

        filter.createdAt?.let {
            conditions.add(
                ChatMembersTable.createdAt eq Instant.from(
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss").parse(it)
                )
            )
        }

        filter.deletedAt?.let {
            conditions.add(
                ChatMembersTable.deletedAt eq Instant.from(
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss").parse(it)
                )
            )
        }

        if (conditions.isEmpty()) {
            ChatMemberDAO.all().map(::daoToModel)
        } else {
            val finalOp = conditions.reduce { acc, op -> acc and op }
            ChatMemberDAO.find(finalOp).map(::daoToModel)
        }
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
            }
        )
    }

    override suspend fun create(entity: ChatMember): Unit = suspendTransaction {
        ChatMemberDAO.new {
            this.idChat = entity.idChat
            this.idUser = entity.idUser
            this.idRole = entity.idRole
            this.createdAt = Instant.now()
        }
    }
}