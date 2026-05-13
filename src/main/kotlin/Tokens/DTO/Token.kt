package Tokens.DTO

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val id: Int? = null,
    val idUser: Int? = null,
    val authToken: String,
    val dateExpire: String,
    val active: Boolean,
    val deletedAt: String? = null
)
