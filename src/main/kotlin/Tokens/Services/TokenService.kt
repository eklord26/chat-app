package Tokens.Services

import Base.Helpers.EncriptionHelper
import Tokens.DTO.Token
import Tokens.DTO.TokenFilter
import Tokens.Repositories.TokenRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

class TokenService {
    private val repo = TokenRepository()

    /**
     * Проверка валидности токена.
     */
    suspend fun checkToken(token: String): Boolean {
        val filter = TokenFilter(authToken = token, active = true)
        val authToken = repo.findByFilter(filter).firstOrNull() ?: return false

        val expireDate = Instant.parse(authToken.dateExpire)
        val now = Instant.now()

        return if (now.isBefore(expireDate)) {
            true
        } else {
            // Деактивируем просроченный токен
            authToken.id?.let { repo.updateById(it, authToken.copy(active = false)) }
            false
        }
    }

    /**
     * Генерация нового набора токенов.
     */
    suspend fun generateAuthToken(): String {
        val authToken = generateToken()
        val encryptToken = generateToken()

        val expireDate = Instant.now().plus(30, ChronoUnit.DAYS)

        val token = Token(
            id = 0,
            authToken = authToken,
            encryptToken = encryptToken,
            dateExpire = expireDate.toString(),
            active = true
        )
        repo.create(token)

        return authToken
    }

    /**
     * Получение ключа шифрования по токену авторизации.
     */
    suspend fun getEncryptToken(token: String): String? {
        val filter = TokenFilter(authToken = token, active = true)
        return repo.findByFilter(filter).firstOrNull()?.encryptToken
    }

    private fun generateToken(): String {
        return EncriptionHelper().generateRandomString()
    }
}