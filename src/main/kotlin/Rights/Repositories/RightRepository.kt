package Rights.Repositories

import Base.Interfaces.IBaseRepository
import Rights.DAO.RightDAO
import Rights.DAO.RightTable
import Rights.DAO.daoToModel
import Rights.DTO.Right
import Rights.DTO.RightFilter
import com.example.Base.Helpers.suspendTransaction
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import java.time.Instant

class RightRepository : IBaseRepository<Right, RightFilter> {
    override suspend fun findById(id: Int): Right? = suspendTransaction {
        daoToModel(RightDAO.findById(id))
    }

    override suspend fun findAll(): List<Right?> = suspendTransaction {
        RightDAO.all().map(::daoToModel)
    }

    override suspend fun findByFilter(filter: RightFilter): List<Right?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.idRole?.let { conditions.add(RightTable.idRole eq it) }

        filter.name?.let { conditions.add(RightTable.name like "%$it%") }

        filter.isDeleted?.let { isDeleted ->
            if (isDeleted) conditions.add(RightTable.deletedAt.isNotNull())
            else conditions.add(RightTable.deletedAt.isNull())
        }

        if (conditions.isEmpty()) {
            RightDAO.all().map(::daoToModel)
        } else {
            val finalOp = conditions.reduce { acc, op -> acc and op }
            RightDAO.find(finalOp).map(::daoToModel)
        }
    }

    override suspend fun updateById(id: Int, entity: Right): Unit = suspendTransaction {
        RightDAO.findByIdAndUpdate(id) {
            it.name = entity.name
            it.idRole = entity.idRole
            it.deletedAt = entity.deletedAt?.let { Instant.parse(it) }
        }
    }

    override suspend fun create(entity: Right): Unit = suspendTransaction {
        RightDAO.new {
            name = entity.name
            idRole = entity.idRole
            deletedAt = null
        }
    }
}