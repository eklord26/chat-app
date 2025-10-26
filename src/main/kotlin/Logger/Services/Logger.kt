package Logger.Services

import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Interfaces.ILogCreating
import Logger.Interfaces.ILogger

class Logger: ILogger {
    private lateinit var logType: LogType
    private lateinit var event: EventType
    private var idUser: Int = 1
    private lateinit var description: String
    private lateinit var idAddress: String

    private lateinit var service: ILogCreating

    override fun create() {
        when (logType) {
            LogType.Warning -> {
                service = WarningLogService()
//                service.createLog(idUser, event, idAddress, description)
            }
            LogType.Error -> {
                service = ErrorLogService()
//                service.createLog(idUser, event, idAddress, description)
            }
            LogType.Event -> {
                service = EventLogService()
//                service.createLog(idUser, event, idAddress, description)
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