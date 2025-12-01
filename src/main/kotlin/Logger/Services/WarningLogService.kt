package Logger.Services

import Logger.DTO.Event
import Logger.DTO.Log
import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Interfaces.ILogRepository
import java.util.*

class WarningLogService: LogService() {
    override var _events: List<Event> = listOf(
        Event(EventType.WARNING_LIMIT_STORAGE, "Предупреждение. Заканчивается место в хранилище"),
        Event(EventType.WARNING_LIMIT_FILES, "Предупреждение. Заканчивается лимит файлов в хранилище"),
        Event(EventType.WARNING_LIMIT_LOGIN, "Предупреждение. Заканчиваются попытки входа"),
        Event(EventType.WARNING_LIMIT_USER_STORAGE, "Предупреждение. Заканчивается место в хранилище пользователя"),
    )

    override val _defaultLifeTime = 90

    override suspend fun createLog(
        repository: ILogRepository,
        idUser: Int,
        event: EventType,
        ip: String,
        description: String
    ) {
        _repository = repository
        _log = Log(
            id = 0,
            idUser = idUser,
            logType = LogType.Warning.name,
            event = event.name,
            lifeTime = _defaultLifeTime,
            ipAddress = ip,
            date = Date().toString(),
            description = if (description == "" && _events.find { _event: Event -> _event.type == event } != null)
            {
                _events.find { _event: Event -> _event.type == event }!!.description
            } else description,
        )
        _repository.create(_log)
    }
}