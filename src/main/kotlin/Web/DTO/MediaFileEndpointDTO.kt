package Web.DTO

import kotlinx.serialization.Serializable

@Serializable
data class MediaFileEndpointDTO(
    val id: Int,
    val fileName: String,
    val extension: String,
    val mimeType: String,
    val mediaType: String,
    val sizeBytes: Long,
    val url: String,
    val createdAt: String
)
