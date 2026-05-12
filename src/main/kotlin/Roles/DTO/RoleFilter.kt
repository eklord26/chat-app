package Roles.DTO

import kotlinx.serialization.Serializable

@Serializable
data class RoleFilter(
    val name: String? = null,
    val isDeleted: Boolean? = null
)