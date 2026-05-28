package Authentication.Controllers

import Authentication.DTO.AuthenticationBodyDTO
import Authentication.Services.AuthenticationService
import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Services.AuditLogWriter
import io.ktor.http.*

import io.ktor.server.application.*
import io.ktor.server.plugins.origin
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.AuthenticationRouter() {
    routing {
        post("/authentication")
        {
            val authData = call.receive<AuthenticationBodyDTO>()
            val auth = AuthenticationService(environment)
            val result = auth.authenticate(authData)
            val status = if (result.status == "success") HttpStatusCode.OK else HttpStatusCode.Unauthorized
            AuditLogWriter.write(
                userId = result.id,
                type = if (result.status == "success") LogType.Event else LogType.Error,
                event = if (result.status == "success") EventType.LOGIN else EventType.ERROR_LOGIN,
                ipAddress = call.request.origin.remoteAddress,
                description = if (result.status == "success") "User logged in" else result.message
            )
            call.respond(status, result)
        }
    }
}
