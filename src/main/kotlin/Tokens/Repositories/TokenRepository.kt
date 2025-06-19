package Tokens.Repositories

import Base.Interfaces.IBaseRepository
import Tokens.DAO.TokenDAO
import Tokens.DAO.TokenTable
import Tokens.DAO.daoToModel
import Tokens.DTO.Token
import Tokens.Interfaces.ITokenRepository
import com.example.Base.Helpers.suspendTransaction
import java.time.Instant
import java.time.format.DateTimeFormatter

class TokenRepository: IBaseRepository<Token>, ITokenRepository {
    override suspend fun findById(id: Int): Token? = suspendTransaction {
        daoToModel(TokenDAO.findById(id))
    }

    override suspend fun findAll(): List<Token?> = suspendTransaction {
        TokenDAO.all().map(::daoToModel)
    }

    override suspend fun updateById(id: Int, entity: Token): Unit = suspendTransaction {
        TokenDAO.findByIdAndUpdate(
            id,
            { it: TokenDAO->
                it.encryptToken = entity.encryptToken
                it.authToken = entity.authToken
                it.dateExpire = Instant.from(
                    DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss").parse(entity.dateExpire)
                )
                it.active = entity.active
            }
        )
    }

    override suspend fun create(entity: Token): Unit = suspendTransaction {
        TokenDAO.new {
            authToken = entity.authToken
            encryptToken = entity.encryptToken
            dateExpire
        }
    }

    override suspend fun findByToken(token: String): List<Token?> = suspendTransaction {
        TokenDAO
            .find { (TokenTable.authToken eq token) }
            .map(::daoToModel)
    }
}