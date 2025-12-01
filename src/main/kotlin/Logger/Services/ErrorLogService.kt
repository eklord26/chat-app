package Logger.Services

import Logger.DTO.Event
import Logger.DTO.Log
import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Interfaces.ILogRepository
import java.util.*

class ErrorLogService: LogService() {

   override var _events: List<Event> = listOf(
       Event(EventType.ERROR_LOGIN, "Ошибка входа в систему"),
       Event(EventType.ERROR_STORAGE, "Ошибка сохранения в хранилище"),
       Event(EventType.ERROR_MESSAGE, "Ошибка отправки сообщения"),
       Event(EventType.ERROR_LOGOUT, "Ошибка выхода из системы"),
       Event(EventType.ERROR_REGISTER, "Ошибка регистрации в системе"),
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
            logType = LogType.Error.name,
            event = event.name,
            lifeTime = _defaultLifeTime,
            ipAddress = ip,
            date = Date().toString(),
            description = if (description == "" && _events.find { _event: Event -> _event.type == event } != null)
            {
                _events.find { _event: Event -> _event.type == event }!!.description
            } else description,
        )
    }
}