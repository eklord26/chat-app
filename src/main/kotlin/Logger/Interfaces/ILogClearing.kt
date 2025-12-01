package Logger.Interfaces

import Logger.DTO.Log

interface ILogClearing {
    public suspend fun clearLogs(repository: ILogRepository, logs: List<Log>): Unit
}