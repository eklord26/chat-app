package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class AuditLogEndpointDTO(
    val id: Int,
    val type: String,
    val event: String,
    val userId: Int,
    val date: String,
    val description: String,
    val ipAddress: String,
    val lifeTime: Int
)
