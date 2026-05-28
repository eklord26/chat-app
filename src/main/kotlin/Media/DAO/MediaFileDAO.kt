package Media.DAO

import Media.DTO.MediaFile
import com.example.Users.DAO.UserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object MediaFileTable : IntIdTable("media_files") {
    val uploaderUserId = reference("uploader_user_id", UserTable)
    val originalFileName = text("original_file_name")
    val storedFileName = text("stored_file_name")
    val extension = varchar("extension", 32)
    val mimeType = varchar("mime_type", 128)
    val mediaType = varchar("media_type", 32)
    val sizeBytes = long("size_bytes")
    val storagePath = text("storage_path")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val deletedAt = timestamp("deleted_at").nullable()
}

class MediaFileDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MediaFileDAO>(MediaFileTable)

    var uploaderUserId by MediaFileTable.uploaderUserId
    var originalFileName by MediaFileTable.originalFileName
    var storedFileName by MediaFileTable.storedFileName
    var extension by MediaFileTable.extension
    var mimeType by MediaFileTable.mimeType
    var mediaType by MediaFileTable.mediaType
    var sizeBytes by MediaFileTable.sizeBytes
    var storagePath by MediaFileTable.storagePath
    var createdAt by MediaFileTable.createdAt
    var deletedAt by MediaFileTable.deletedAt
}

fun daoToModel(dao: MediaFileDAO?): MediaFile? = dao?.let {
    MediaFile(
        id = it.id.value,
        uploaderUserId = it.uploaderUserId.value,
        originalFileName = it.originalFileName,
        storedFileName = it.storedFileName,
        extension = it.extension,
        mimeType = it.mimeType,
        mediaType = it.mediaType,
        sizeBytes = it.sizeBytes,
        storagePath = it.storagePath,
        createdAt = it.createdAt.toString(),
        deletedAt = it.deletedAt?.toString()
    )
}
