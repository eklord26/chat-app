package Registration.Controllers

import Registration.RegisterBodyDTO
import Registration.Services.RegistrationService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
fun Application.RegistrationRouter() {
    routing {
        post("/register")
        {
            val registerData = call.receive<RegisterBodyDTO>()
            val reg = RegistrationService()
            call.respond(reg.register(registerData))
        }
        get("/live")
        {
            call.respond(true)
        }
    }
}