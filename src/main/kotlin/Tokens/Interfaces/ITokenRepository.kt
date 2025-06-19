package Tokens.Interfaces

import Tokens.DTO.Token

interface ITokenRepository {
    suspend fun findByToken(token: String): List<Token?>
}