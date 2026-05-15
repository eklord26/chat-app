package Tokens.Repositories

import Base.Interfaces.IBaseRepository
import Tokens.DAO.TokenDAO
import Tokens.DAO.TokenTable
import Tokens.DAO.daoToModel
import Tokens.DTO.Token
import Tokens.DTO.TokenFilter
import com.example.Base.Helpers.suspendTransaction
import com.example.Users.DAO.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import java.time.Instant

class TokenRepository : IBaseRepository<Token, TokenFilter> {
    override suspend fun findById(id: Int): Token? = suspendTransaction {
        daoToModel(TokenDAO.findById(id))
    }

    override suspend fun findAll(): List<Token?> = suspendTransaction {
        TokenDAO.all().map(::daoToModel)
    }

    override suspend fun findByFilter(filter: TokenFilter): List<Token?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.idUser?.let { conditions.add(TokenTable.idUser eq EntityID(it, UserTable)) }
        filter.authToken?.let { conditions.add(TokenTable.authToken eq it) }
        filter.active?.let { conditions.add(TokenTable.active eq it) }

        filter.isExpired?.let { expired ->
            val now = Instant.now()
            if (expired) conditions.add(TokenTable.dateExpire less now)
            else conditions.add(TokenTable.dateExpire greaterEq now)
        }

        filter.isDeleted?.let { isDeleted ->
            if (isDeleted) conditions.add(TokenTable.deletedAt.isNotNull())
            else conditions.add(TokenTable.deletedAt.isNull())
        }

        if (conditions.isEmpty()) {
            TokenDAO.all().map(::daoToModel)
        } else {
            val finalOp = conditions.reduce { acc, op -> acc and op }
            TokenDAO.find(finalOp).map(::daoToModel)
        }
    }

    override suspend fun updateById(id: Int, entity: Token): Unit = suspendTransaction {
        TokenDAO.findByIdAndUpdate(id) {
            it.idUser = entity.idUser?.let { idUser -> EntityID(idUser, UserTable) }
            it.authToken = entity.authToken
            it.dateExpire = Instant.parse(entity.dateExpire)
            it.active = entity.active
            it.deletedAt = entity.deletedAt?.let { date -> Instant.parse(date) }
        }
    }

    override suspend fun create(entity: Token): Unit = suspendTransaction {
        TokenDAO.new {
            idUser = entity.idUser?.let { idUser -> EntityID(idUser, UserTable) }
            authToken = entity.authToken
            dateExpire = Instant.parse(entity.dateExpire)
            active = entity.active
            deletedAt = null
        }
    }
}
