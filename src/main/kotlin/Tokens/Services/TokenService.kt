package Tokens.Services

import Base.Helpers.EncriptionHelper
import Tokens.DTO.Token
import Tokens.Repositories.TokenRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TokenService {

    private val repo = TokenRepository()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun checkToken(token: String): Boolean {
        val authToken = repo.findByToken(token).firstOrNull() ?: return false
        if (!authToken.active) return false

        val expireDate = ZonedDateTime.parse(authToken.dateExpire)
        val currentDate = ZonedDateTime.now(ZoneId.systemDefault())

        return if (currentDate.isBefore(expireDate)) {
            true
        } else {
            repo.updateById(authToken.id, authToken.copy(active = false))
            false
        }
    }

    public suspend fun generateAuthToken(): String {

        val authToken = generateToken()
        val encryptToken = generateToken()

        val expireDate = LocalDateTime.now(ZoneId.systemDefault())
            .plusDays(30)
            .format(formatter)
        val token = Token(
            id = 0,
            authToken = authToken,
            encryptToken = encryptToken,
            dateExpire = expireDate,
            active = true
        )
        repo.create(token)

        return authToken
    }

    public suspend fun getEncryptToken(token: String): String? {
        return repo.findByToken(token).firstOrNull()?.encryptToken
    }

    private fun generateToken(): String {
        val helper = EncriptionHelper()
        return helper.generateRandomString()
    }
}