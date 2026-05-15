package Authentication.Controllers

import Authentication.DTO.AuthenticationBodyDTO
import Authentication.Services.AuthenticationService
import io.ktor.http.*

import io.ktor.server.application.*
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
            call.respond(status, result)
        }
    }
}
