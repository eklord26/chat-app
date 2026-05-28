package Tokens.Services

import io.ktor.server.application.ApplicationCall

object AuthTokenExtractor {
    fun extract(call: ApplicationCall): String? {
        val authorization = call.request.headers["Authorization"]
        if (!authorization.isNullOrBlank() && authorization.startsWith("Bearer ", ignoreCase = true)) {
            return authorization.substringAfter(" ").trim().takeIf { it.isNotBlank() }
        }

        return call.request.headers["Auth-Token"]?.takeIf { it.isNotBlank() }
            ?: call.request.queryParameters["token"]?.takeIf { it.isNotBlank() }
    }
}
