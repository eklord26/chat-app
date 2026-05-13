package Log.Controllers

import Tokens.Services.AuthGuard
import Logger.DAO.LogTable.logType
import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Interfaces.ILogCreating
import Logger.Interfaces.ILogRepository
import Logger.Interfaces.ILogSearching
import Logger.Interfaces.ILogger
import Logger.Repositories.LogRepository
import Logger.Services.EventLogService
import Logger.Services.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
fun Application.LogRouter() {
    val authGuard = AuthGuard()

    routing {
        get("/logs")
        {
            authGuard.requireUserId(call) ?: return@get
            val logs: ILogSearching = EventLogService()
            val repo: ILogRepository = LogRepository()
            call.respond(logs.getAllLogs(repo))
        }
        get("/logs/create")
        {
            val idUser = authGuard.requireUserId(call) ?: return@get
            val logger: ILogger = Logger()

            logger.setLogType(LogType.Event)
            logger.setUserId(idUser)
            logger.setEventType(EventType.LOGIN)
            logger.setIpAddress(call.request.origin.remoteAddress)
            logger.setDescription("Тестовое событие ${(0..100).random()}")
            logger.create()
            call.respond(HttpStatusCode.OK)
        }
    }
}
