package Users.DTO

import kotlinx.serialization.Serializable

@Serializable
data class UserFilter(
    val login: String? = null,
    val name: String? = null,
    val isAdmin: Boolean? = null,
    val deleted: Boolean? = null
)