package Logger.Services

import Logger.DTO.Event
import Logger.DTO.Log
import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Interfaces.ILogClearing
import Logger.Interfaces.ILogCreating
import Logger.Interfaces.ILogRepository
import Logger.Interfaces.ILogSearching

abstract class LogService: ILogCreating, ILogSearching, ILogClearing {

    protected lateinit var _log: Log
    abstract var _events: List<Event>
    abstract val _defaultLifeTime: Int
    protected lateinit var _repository: ILogRepository

    public abstract override suspend fun createLog (
            repository: ILogRepository,
            idUser: Int,
            event: EventType,
            ip: String,
            description: String,
        )

    public override suspend fun getAllLogs(repository: ILogRepository): List<Log?> {
        _repository = repository
        return _repository.findAll()
    }

    public override suspend fun getLogByType(repository: ILogRepository, type: LogType): List<Log?> {
        _repository = repository
        return _repository.findByType(type)
    }

    public override suspend fun getLogByIpAddress(repository: ILogRepository, ipAddress: String): List<Log?> {
        _repository = repository
        return _repository.findByIp(ipAddress)
    }

    public override suspend fun getLogByEvent(repository: ILogRepository, event: EventType): List<Log?> {
        _repository = repository
        return _repository.findByEvent(event)
    }

    public override suspend fun getLogByDates(repository: ILogRepository, dateFrom: String, dateTo: String): List<Log?> {
        _repository = repository
        return _repository.findByDatePeriod(dateFrom, dateTo)
    }

    public override suspend fun clearLogs(repository: ILogRepository, logs: List<Log>): Unit {
        _repository = repository
        for (log in logs) _repository.delete(log.id)
    }
}