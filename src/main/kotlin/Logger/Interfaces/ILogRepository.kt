package Logger.Interfaces

import Logger.DTO.Event
import Logger.DTO.Log
import Logger.Enums.EventType
import Logger.Enums.LogType

interface ILogRepository {
    public suspend fun findAll(): List<Log?>
    public suspend fun findByType(type: LogType): List<Log?>
    public suspend fun findByEvent(event: EventType): List<Log?>
    public suspend fun findByIdUser(id: Int): List<Log?>
    public suspend fun findByIp(ip: String): List<Log?>
    public suspend fun findByDatePeriod(dateFrom: String, dateTo: String): List<Log?>
    public suspend fun create(log: Log): Unit
    public suspend fun delete(id: Int): Unit
}