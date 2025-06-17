package com.example

import Base.Application.Configs
import com.example.Users.Repository.UserRepository
import Passwords.Builders.PasswordHashBuilder
import Passwords.Interfaces.IPasswordHashBuilder
import Passwords.Services.PasswordService
import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.security.SecureRandom

fun Application.configureRouting() {
    var service = PasswordService()
    var hash = service.createHashPassword("1234562")
    var checking = service.checkHashPassword(hash, "1234561")
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/test") {

            call.respond(checking.toString())
        }

        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}
