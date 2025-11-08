package Logger.Services

import Logger.DTO.Event
import Logger.DTO.Log
import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Interfaces.ILogRepository
import java.util.*

class EventLogService: LogService() {

    override var _events: List<Event> = listOf(
        Event(EventType.LOGIN, "Вход в систему"),
        Event(EventType.LOGOUT, "Выход из системы"),
        Event(EventType.ADD_CHAT_MEMBER, "Добавление участника чата"),
        Event(EventType.DELETE_CHAT_MEMBER, "Удаление участника чата"),
        Event(EventType.UPDATE_CHAT_MEMBER, "Изменение участника чата"),
        Event(EventType.ADD_ROLE, "Создание новой роли в чате"),
        Event(EventType.UPDATE_ROLE, "Изменение роли в чате"),
        Event(EventType.DELETE_ROLE, "Удаление роли в чате"),
        Event(EventType.NEW_USER, "СОздание нового пользователя"),
        Event(EventType.DELETE_USER, "Удаление пользователя"),
        Event(EventType.NEW_CHAT, "Создание нового чата"),
        Event(EventType.DELETE_CHAT, "Удаление чата"),
        Event(EventType.USER_CHANGE_PASSWORD, "Пользователь сменяет пароль"),
        Event(EventType.START_CALL, "Начало звонка"),
        Event(EventType.END_CALL, "Конец звонка"),
    )

    override val _defaultLifeTime = 150

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
            logType = LogType.Event.name,
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