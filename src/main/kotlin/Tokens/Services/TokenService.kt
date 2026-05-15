package Tokens.Services

import Tokens.DTO.Token
import Tokens.DTO.TokenFilter
import Tokens.Repositories.TokenRepository
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

class TokenService {
    private val repo = TokenRepository()
    private val secureRandom = SecureRandom()

    suspend fun checkToken(token: String): Boolean {
        val authToken = findActiveToken(token) ?: return false
        val expireDate = Instant.parse(authToken.dateExpire)

        return if (Instant.now().isBefore(expireDate)) {
            true
        } else {
            authToken.id?.let { repo.updateById(it, authToken.copy(active = false)) }
            false
        }
    }

    suspend fun getUserIdByToken(token: String): Int? {
        if (!checkToken(token)) return null
        return findActiveToken(token)?.idUser
    }

    suspend fun generateAuthToken(idUser: Int): String {
        val authToken = generateToken()
        val expireDate = Instant.now().plus(30, ChronoUnit.DAYS)

        repo.create(
            Token(
                id = 0,
                idUser = idUser,
                authToken = authToken,
                dateExpire = expireDate.toString(),
                active = true
            )
        )

        return authToken
    }

    private suspend fun findActiveToken(token: String): Token? {
        val filter = TokenFilter(authToken = token, active = true, isDeleted = false)
        return repo.findByFilter(filter).firstOrNull()
    }

    private fun generateToken(): String {
        val bytes = ByteArray(TOKEN_SIZE_BYTES)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private companion object {
        const val TOKEN_SIZE_BYTES = 32
    }
}
