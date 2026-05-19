package Contacts.Repositories

import Base.Interfaces.IBaseRepository
import Contacts.DAO.ContactDAO
import Contacts.DAO.ContactTable
import Contacts.DAO.daoToModel
import Contacts.DTO.Contact
import Contacts.DTO.ContactFilter
import com.example.Base.Helpers.suspendTransaction
import com.example.Users.DAO.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import java.time.Instant

class ContactRepository : IBaseRepository<Contact, ContactFilter> {
    override suspend fun findById(id: Int): Contact? = suspendTransaction {
        daoToModel(ContactDAO.findById(id))
    }

    override suspend fun findAll(): List<Contact?> = suspendTransaction {
        ContactDAO.all().map(::daoToModel)
    }

    override suspend fun findByFilter(filter: ContactFilter): List<Contact?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.ownerUserId?.let { conditions.add(ContactTable.ownerUserId eq EntityID(it, UserTable)) }
        filter.contactUserId?.let { conditions.add(ContactTable.contactUserId eq EntityID(it, UserTable)) }
        filter.isDeleted?.let { isDeleted ->
            if (isDeleted) conditions.add(ContactTable.deletedAt.isNotNull())
            else conditions.add(ContactTable.deletedAt.isNull())
        }

        if (conditions.isEmpty()) {
            ContactDAO.all().map(::daoToModel)
        } else {
            ContactDAO.find(conditions.reduce { acc, op -> acc and op }).map(::daoToModel)
        }
    }

    override suspend fun updateById(id: Int, entity: Contact): Unit = suspendTransaction {
        ContactDAO.findByIdAndUpdate(id) {
            it.ownerUserId = EntityID(entity.ownerUserId, UserTable)
            it.contactUserId = EntityID(entity.contactUserId, UserTable)
            it.displayName = entity.displayName
            it.deletedAt = entity.deletedAt?.let { date -> Instant.parse(date) }
        }
    }

    override suspend fun create(entity: Contact): Unit = suspendTransaction {
        ContactDAO.new {
            ownerUserId = EntityID(entity.ownerUserId, UserTable)
            contactUserId = EntityID(entity.contactUserId, UserTable)
            displayName = entity.displayName
            createdAt = Instant.now()
            deletedAt = null
        }
    }
}
