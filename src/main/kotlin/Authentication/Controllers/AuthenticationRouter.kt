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
            val auth = AuthenticationService()
            val token = call.request.headers["Auth-Token"]

            if (token != null) {
                call.respond(auth.authenticate(authData, token))
            } else call.respond(HttpStatusCode.Unauthorized)
        }
    }
}