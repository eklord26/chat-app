package Logger.Interfaces

import Logger.DTO.Log
import Logger.Enums.EventType
import Logger.Enums.LogType

interface ILogSearching {
    public suspend fun getAllLogs(repository: ILogRepository): List<Log?>
    public suspend fun getLogByType(repository: ILogRepository, type: LogType): List<Log?>
    public suspend fun getLogByIpAddress(repository: ILogRepository, ipAddress: String): List<Log?>
    public suspend fun getLogByEvent(repository: ILogRepository, event: EventType): List<Log?>
    public suspend fun getLogByDates(repository: ILogRepository, dateFrom: String, dateTo: String): List<Log?>
}