package Media.DTO

import kotlinx.serialization.Serializable

@Serializable
data class MediaFile(
    val id: Int? = null,
    val uploaderUserId: Int,
    val originalFileName: String,
    val storedFileName: String,
    val extension: String,
    val mimeType: String,
    val mediaType: String,
    val sizeBytes: Long,
    val storagePath: String,
    val createdAt: String,
    val deletedAt: String? = null
)
