package Tokens.Services

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

class AuthGuard(private val tokenService: TokenService = TokenService()) {
    suspend fun requireUserId(call: ApplicationCall): Int? {
        val token = AuthTokenExtractor.extract(call)
        val userId = token?.let { tokenService.getUserIdByToken(it) }

        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized)
        }

        return userId
    }
}
