package Rights.DTO

import kotlinx.serialization.Serializable

@Serializable
data class Right(
    val id: Int,
    val idRole: Int,
    val name: String,
    val deletedAt: String? = null
)