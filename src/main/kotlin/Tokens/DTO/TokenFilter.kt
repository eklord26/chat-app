package Tokens.DTO

import kotlinx.serialization.Serializable

@Serializable
data class TokenFilter(
    val authToken: String? = null,
    val encryptToken: String? = null,
    val active: Boolean? = null,
    val isExpired: Boolean? = null
)