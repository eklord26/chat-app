package Logger.Interfaces

import Logger.Enums.EventType
import Logger.Enums.LogType

interface ILogger {
    public fun create(): Unit
    public fun setLogType(type: LogType): Unit
    public fun setEventType(type: EventType): Unit
    public fun setUserId(userId: Int): Unit
    public fun setIpAddress(ipAddress: String): Unit
    public fun setDescription(message: String): Unit
}