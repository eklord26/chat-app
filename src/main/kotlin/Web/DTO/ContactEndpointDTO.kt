package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ContactEndpointDTO(
    val id: Int,
    val ownerUserId: Int,
    val contactUserId: Int,
    val displayName: String? = null,
    val contact: UserEndpointDTO? = null,
    val createdAt: String,
    val deletedAt: String? = null
)
