package Tokens.DTO

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val id: Int,
    val authToken: String,
    val encryptToken: String,
    val dateExpire: String,
    val active: Boolean
)