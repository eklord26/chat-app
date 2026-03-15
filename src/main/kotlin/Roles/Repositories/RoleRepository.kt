package Roles.Repositories

import Base.Interfaces.IBaseRepository
import Roles.DAO.RoleDAO
import Roles.DAO.RoleTable
import Roles.DAO.daoToModel
import Roles.DTO.Role
import Roles.DTO.RoleFilter
import com.example.Base.Helpers.suspendTransaction
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and

class RoleRepository : IBaseRepository<Role, RoleFilter> {
    override suspend fun findById(id: Int): Role? = suspendTransaction {
        daoToModel(RoleDAO.findById(id))
    }

    override suspend fun findByFilter(filter: RoleFilter): List<Role?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.name?.let {
            conditions.add(RoleTable.name like "%$it%")
        }

        filter.isDeleted?.let { deleted ->
            if (deleted) {
                conditions.add(RoleTable.deletedAt.isNotNull())
            } else {
                conditions.add(RoleTable.deletedAt.isNull())
            }
        }

        if (conditions.isEmpty()) {
            RoleDAO.all().map(::daoToModel)
        } else {
            val finalOp = conditions.reduce { acc, op -> acc and op }
            RoleDAO.find(finalOp).map(::daoToModel)
        }
    }

    override suspend fun findAll(): List<Role?> = suspendTransaction {
        RoleDAO.all().map(::daoToModel)
    }

    override suspend fun create(entity: Role): Unit = suspendTransaction {
        RoleDAO.new {
            name = entity.name
            deletedAt = entity.deletedAt?.let { java.time.Instant.parse(it) }
        }
    }

    override suspend fun updateById(id: Int, entity: Role): Unit = suspendTransaction {
        RoleDAO.findByIdAndUpdate(id) {
            it.name = entity.name
            it.deletedAt = entity.deletedAt?.let { date -> java.time.Instant.parse(date) }
        }
    }
}