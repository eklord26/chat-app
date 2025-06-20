package Registration.Controllers

import Registration.DTO.RegisterBodyDTO
import Registration.Services.RegistrationService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.RegistrationRouter() {
    routing {
        post("/register")
        {
            val registerData = call.receive<RegisterBodyDTO>()
            val reg = RegistrationService()
            call.respond(reg.register(registerData))
        }
    }
}