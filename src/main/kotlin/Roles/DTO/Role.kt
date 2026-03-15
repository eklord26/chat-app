package Roles.DTO

import kotlinx.serialization.Serializable

@Serializable
data class Role(
    val id: Int,
    val name: String,
    val deletedAt: String? = null
)