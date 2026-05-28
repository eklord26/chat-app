package Media.Repositories

import Media.DAO.MediaFileDAO
import Media.DAO.MediaFileTable
import Media.DAO.daoToModel
import Media.DTO.MediaFile
import com.example.Base.Helpers.suspendTransaction
import com.example.Users.DAO.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import java.time.Instant

class MediaFileRepository {
    suspend fun findById(id: Int): MediaFile? = suspendTransaction {
        daoToModel(MediaFileDAO.findById(id))
    }

    suspend fun findActiveByIds(ids: Collection<Int>): List<MediaFile> = suspendTransaction {
        if (ids.isEmpty()) return@suspendTransaction emptyList()
        MediaFileDAO
            .find { MediaFileTable.deletedAt.isNull() }
            .mapNotNull(::daoToModel)
            .filter { it.id in ids }
    }

    suspend fun activeStorageUsageBytes(): Long = suspendTransaction {
        MediaFileDAO
            .find { MediaFileTable.deletedAt.isNull() }
            .sumOf { it.sizeBytes }
    }

    suspend fun create(entity: MediaFile): MediaFile? = suspendTransaction {
        val dao = MediaFileDAO.new {
            uploaderUserId = EntityID(entity.uploaderUserId, UserTable)
            originalFileName = entity.originalFileName
            storedFileName = entity.storedFileName
            extension = entity.extension
            mimeType = entity.mimeType
            mediaType = entity.mediaType
            sizeBytes = entity.sizeBytes
            storagePath = entity.storagePath
            createdAt = Instant.now()
            deletedAt = null
        }
        daoToModel(dao)
    }
}
