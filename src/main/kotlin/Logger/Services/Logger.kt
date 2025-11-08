package Logger.Services

import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Interfaces.ILogCreating
import Logger.Interfaces.ILogRepository
import Logger.Interfaces.ILogger
import Logger.Repositories.LogRepository
import io.ktor.server.application.*
import org.slf4j.LoggerFactory

class Logger: ILogger {
    private lateinit var logType: LogType
    private lateinit var event: EventType
    private lateinit var description: String
    private lateinit var idAddress: String

    private lateinit var service: ILogCreating

    private var idUser: Int = 1
    private val repo: ILogRepository = LogRepository()

    override suspend fun create() {
        val logger = LoggerFactory.getLogger(Application::class.java)
        logger.debug(logType.toString())
        when (logType) {
            LogType.Warning -> {
                service = WarningLogService()
                service.createLog(repo, idUser, event, idAddress, description)
            }
            LogType.Error -> {
                service = ErrorLogService()
                service.createLog(repo, idUser, event, idAddress, description)
            }
            LogType.Event -> {
                service = EventLogService()
                service.createLog(repo, idUser, event, idAddress, description)
            }
        }
    }

    override fun setLogType(type: LogType): Unit {
        logType = type
    }

    override fun setEventType(type: EventType): Unit {
        event = type
    }

    override fun setUserId(id: Int): Unit {
        idUser = id
    }

    override fun setIpAddress(ip: String): Unit {
        idAddress = ip
    }

    override fun setDescription(message: String): Unit {
        description = message
    }


}