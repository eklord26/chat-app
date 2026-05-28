package Registration.Controllers

import Registration.DTO.RegisterBodyDTO
import Registration.Services.RegistrationService
import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Services.AuditLogWriter
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.origin
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.RegistrationRouter() {
    routing {
        post("/register")
        {
            val registerData = call.receive<RegisterBodyDTO>()
            val reg = RegistrationService(environment)
            runCatching { reg.register(registerData) }
                .onSuccess {
                    AuditLogWriter.write(
                        userId = it.id,
                        type = if (it.status == "success") LogType.Event else LogType.Error,
                        event = if (it.status == "success") EventType.NEW_USER else EventType.ERROR_REGISTER,
                        ipAddress = call.request.origin.remoteAddress,
                        description = it.message
                    )
                    call.respond(it)
                }
                .onFailure {
                    AuditLogWriter.write(
                        userId = null,
                        type = LogType.Error,
                        event = EventType.ERROR_REGISTER,
                        ipAddress = call.request.origin.remoteAddress,
                        description = it.message ?: "Invalid registration data"
                    )
                    call.respond(HttpStatusCode.BadRequest, it.message ?: "Invalid registration data")
                }
        }
    }
}
