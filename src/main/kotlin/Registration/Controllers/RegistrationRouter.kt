package Registration.Controllers

import Registration.DTO.RegisterBodyDTO
import Registration.Services.RegistrationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
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
                .onSuccess { call.respond(it) }
                .onFailure { call.respond(HttpStatusCode.BadRequest, it.message ?: "Invalid registration data") }
        }
    }
}
