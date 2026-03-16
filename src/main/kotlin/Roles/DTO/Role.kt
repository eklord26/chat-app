package Roles.DTO

import kotlinx.serialization.Serializable

@Serializable
data class Role(
    val id: Int? = null,
    val name: String,
    val deletedAt: String? = null
)