package Logger.Services

import Logger.DTO.Log
import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Repositories.LogRepository
import java.time.Instant

object AuditLogWriter {
    private val repository = LogRepository()

    suspend fun write(
        userId: Int?,
        type: LogType,
        event: EventType,
        ipAddress: String,
        description: String
    ) {
        repository.create(
            Log(
                id = null,
                logType = type.name,
                event = event.name,
                idUser = userId ?: 1,
                date = Instant.now().toString(),
                description = description,
                lifeTime = defaultLifeTime(type),
                ipAddress = ipAddress.ifBlank { "unknown" }
            )
        )
    }

    private fun defaultLifeTime(type: LogType): Int = when (type) {
        LogType.Event -> 150
        LogType.Warning -> 90
        LogType.Error -> 90
    }
}
