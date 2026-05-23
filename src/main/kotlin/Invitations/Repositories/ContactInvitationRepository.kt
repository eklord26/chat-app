package Invitations.Repositories

import Base.Interfaces.IBaseRepository
import Invitations.DAO.ContactInvitationDAO
import Invitations.DAO.ContactInvitationTable
import Invitations.DAO.daoToModel
import Invitations.DTO.ContactInvitation
import Invitations.DTO.ContactInvitationFilter
import Invitations.Enums.InvitationStatusEnum
import com.example.Base.Helpers.suspendTransaction
import com.example.Users.DAO.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import java.time.Instant

class ContactInvitationRepository : IBaseRepository<ContactInvitation, ContactInvitationFilter> {
    override suspend fun findById(id: Int): ContactInvitation? = suspendTransaction {
        daoToModel(ContactInvitationDAO.findById(id))
    }

    override suspend fun findAll(): List<ContactInvitation?> = suspendTransaction {
        ContactInvitationDAO.all().map(::daoToModel)
    }

    override suspend fun findByFilter(filter: ContactInvitationFilter): List<ContactInvitation?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.senderUserId?.let { conditions.add(ContactInvitationTable.senderUserId eq EntityID(it, UserTable)) }
        filter.receiverUserId?.let { conditions.add(ContactInvitationTable.receiverUserId eq EntityID(it, UserTable)) }
        InvitationStatusEnum.normalize(filter.status)?.let { conditions.add(ContactInvitationTable.status eq it) }
        filter.isDeleted?.let { isDeleted ->
            if (isDeleted) conditions.add(ContactInvitationTable.deletedAt.isNotNull())
            else conditions.add(ContactInvitationTable.deletedAt.isNull())
        }

        if (conditions.isEmpty()) {
            ContactInvitationDAO.all().map(::daoToModel)
        } else {
            ContactInvitationDAO.find(conditions.reduce { acc, op -> acc and op }).map(::daoToModel)
        }
    }

    override suspend fun updateById(id: Int, entity: ContactInvitation): Unit = suspendTransaction {
        ContactInvitationDAO.findByIdAndUpdate(id) {
            it.senderUserId = EntityID(entity.senderUserId, UserTable)
            it.receiverUserId = EntityID(entity.receiverUserId, UserTable)
            it.status = InvitationStatusEnum.normalize(entity.status) ?: entity.status
            it.message = entity.message
            it.respondedAt = entity.respondedAt?.let { date -> Instant.parse(date) }
            it.deletedAt = entity.deletedAt?.let { date -> Instant.parse(date) }
        }
    }

    override suspend fun create(entity: ContactInvitation): Unit = suspendTransaction {
        ContactInvitationDAO.new {
            senderUserId = EntityID(entity.senderUserId, UserTable)
            receiverUserId = EntityID(entity.receiverUserId, UserTable)
            status = InvitationStatusEnum.normalize(entity.status) ?: InvitationStatusEnum.PENDING.value
            message = entity.message
            createdAt = Instant.now()
            respondedAt = null
            deletedAt = null
        }
    }
}
