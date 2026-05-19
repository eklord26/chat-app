package Invitations.Repositories

import Base.Interfaces.IBaseRepository
import Chats.DAO.ChatTable
import Invitations.DAO.ChatInvitationDAO
import Invitations.DAO.ChatInvitationTable
import Invitations.DAO.daoToModel
import Invitations.DTO.ChatInvitation
import Invitations.DTO.ChatInvitationFilter
import Invitations.Enums.InvitationStatusEnum
import Roles.DAO.RoleTable
import com.example.Base.Helpers.suspendTransaction
import com.example.Users.DAO.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import java.time.Instant

class ChatInvitationRepository : IBaseRepository<ChatInvitation, ChatInvitationFilter> {
    override suspend fun findById(id: Int): ChatInvitation? = suspendTransaction {
        daoToModel(ChatInvitationDAO.findById(id))
    }

    override suspend fun findAll(): List<ChatInvitation?> = suspendTransaction {
        ChatInvitationDAO.all().map(::daoToModel)
    }

    override suspend fun findByFilter(filter: ChatInvitationFilter): List<ChatInvitation?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.idChat?.let { conditions.add(ChatInvitationTable.idChat eq EntityID(it, ChatTable)) }
        filter.inviterUserId?.let { conditions.add(ChatInvitationTable.inviterUserId eq EntityID(it, UserTable)) }
        filter.inviteeUserId?.let { conditions.add(ChatInvitationTable.inviteeUserId eq EntityID(it, UserTable)) }
        InvitationStatusEnum.normalize(filter.status)?.let { conditions.add(ChatInvitationTable.status eq it) }
        filter.isDeleted?.let { isDeleted ->
            if (isDeleted) conditions.add(ChatInvitationTable.deletedAt.isNotNull())
            else conditions.add(ChatInvitationTable.deletedAt.isNull())
        }

        if (conditions.isEmpty()) {
            ChatInvitationDAO.all().map(::daoToModel)
        } else {
            ChatInvitationDAO.find(conditions.reduce { acc, op -> acc and op }).map(::daoToModel)
        }
    }

    override suspend fun updateById(id: Int, entity: ChatInvitation): Unit = suspendTransaction {
        ChatInvitationDAO.findByIdAndUpdate(id) {
            it.idChat = EntityID(entity.idChat, ChatTable)
            it.inviterUserId = EntityID(entity.inviterUserId, UserTable)
            it.inviteeUserId = EntityID(entity.inviteeUserId, UserTable)
            it.idRole = EntityID(entity.idRole, RoleTable)
            it.status = InvitationStatusEnum.normalize(entity.status) ?: entity.status
            it.message = entity.message
            it.respondedAt = entity.respondedAt?.let { date -> Instant.parse(date) }
            it.deletedAt = entity.deletedAt?.let { date -> Instant.parse(date) }
        }
    }

    override suspend fun create(entity: ChatInvitation): Unit = suspendTransaction {
        ChatInvitationDAO.new {
            idChat = EntityID(entity.idChat, ChatTable)
            inviterUserId = EntityID(entity.inviterUserId, UserTable)
            inviteeUserId = EntityID(entity.inviteeUserId, UserTable)
            idRole = EntityID(entity.idRole, RoleTable)
            status = InvitationStatusEnum.normalize(entity.status) ?: InvitationStatusEnum.PENDING.value
            message = entity.message
            createdAt = Instant.now()
            respondedAt = null
            deletedAt = null
        }
    }
}
