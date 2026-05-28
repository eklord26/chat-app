package Media.Services

import Media.DTO.MediaFile
import Media.Enums.MediaTypeEnum
import Media.Repositories.MediaFileRepository
import Web.DTO.MediaFileEndpointDTO
import Web.Endpoints.toEndpointDTO
import io.ktor.http.content.PartData
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.UUID
import kotlin.io.path.absolute
import kotlin.io.path.name

class MediaService(
    environment: ApplicationEnvironment? = null,
    private val repository: MediaFileRepository = MediaFileRepository()
) {
    private val maxFileSizeBytes = mbToBytes(
        environment?.config?.propertyOrNull("media.maxFileSizeMb")?.getString()?.toLongOrNull() ?: 30L
    )
    private val serverStorageLimitBytes = mbToBytes(
        environment?.config?.propertyOrNull("media.serverStorageLimitMb")?.getString()?.toLongOrNull() ?: 5120L
    )
    private val storageRoot = Path.of(
        environment?.config?.propertyOrNull("media.storagePath")?.getString() ?: "./uploads/media"
    ).absolute().normalize()

    suspend fun saveUploadedFile(uploaderUserId: Int, filePart: PartData.FileItem): MediaFileEndpointDTO {
        val originalFileName = filePart.originalFileName?.trim().orEmpty()
        val extension = extractExtension(originalFileName)
        val mediaType = MediaTypeEnum.byExtension(extension)
            ?: throw MediaValidationException("UNSUPPORTED_EXTENSION", "Unsupported file extension")

        val bytes = filePart.provider().readRemaining().readByteArray()
        val sizeBytes = bytes.size.toLong()
        if (sizeBytes > maxFileSizeBytes) {
            throw MediaValidationException("FILE_TOO_LARGE", "File size exceeds configured limit")
        }

        val currentUsage = repository.activeStorageUsageBytes()
        if (currentUsage + sizeBytes > serverStorageLimitBytes) {
            throw MediaValidationException("SERVER_STORAGE_LIMIT_EXCEEDED", "Server storage limit exceeded")
        }

        Files.createDirectories(storageRoot)
        val storedFileName = "${UUID.randomUUID()}.$extension"
        val target = storageRoot.resolve(storedFileName).normalize()
        if (!target.startsWith(storageRoot)) {
            throw MediaValidationException("INVALID_STORAGE_PATH", "Invalid storage path")
        }
        Files.write(target, bytes)

        val media = repository.create(
            MediaFile(
                uploaderUserId = uploaderUserId,
                originalFileName = originalFileName,
                storedFileName = target.fileName.name,
                extension = extension,
                mimeType = filePart.contentType?.toString() ?: defaultMimeType(mediaType),
                mediaType = mediaType.value,
                sizeBytes = sizeBytes,
                storagePath = target.toString(),
                createdAt = Instant.now().toString()
            )
        ) ?: throw MediaValidationException("MEDIA_CREATE_FAILED", "Media file metadata was not created")

        return media.toEndpointDTO()
    }

    suspend fun findById(id: Int): MediaFile? = repository.findById(id)

    suspend fun findActiveByIds(ids: Collection<Int>): List<MediaFile> = repository.findActiveByIds(ids)

    private fun extractExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        if (fileName.isBlank() || dotIndex < 0 || dotIndex == fileName.lastIndex) {
            throw MediaValidationException("FILE_EXTENSION_REQUIRED", "File extension is required")
        }
        return fileName.substring(dotIndex + 1).lowercase()
    }

    private fun defaultMimeType(mediaType: MediaTypeEnum): String = when (mediaType) {
        MediaTypeEnum.PHOTO -> "image/*"
        MediaTypeEnum.VIDEO -> "video/*"
        MediaTypeEnum.DOCUMENT -> "application/octet-stream"
    }

    private fun mbToBytes(value: Long): Long = value * 1024L * 1024L
}

class MediaValidationException(
    val code: String,
    override val message: String
) : RuntimeException(message)
