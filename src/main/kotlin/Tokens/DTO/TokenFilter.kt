package Tokens.DTO

import kotlinx.serialization.Serializable

@Serializable
data class TokenFilter(
    val idUser: Int? = null,
    val authToken: String? = null,
    val encryptToken: String? = null,
    val active: Boolean? = null,
    val isExpired: Boolean? = null,
    val isDeleted: Boolean? = null
)
