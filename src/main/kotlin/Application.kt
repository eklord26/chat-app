package com.example

import Authentication.Controllers.AuthenticationRouter
import Registration.Controllers.RegistrationRouter
import Users.Controllers.UserRouting
import configureDatabases
import configureRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    configureSockets()
    configureSerialization()
    configureDatabases()
    configureMonitoring()
    configureRouting()
    UserRouting()
    RegistrationRouter()
    AuthenticationRouter()
}

fun Application.getApplication(): Application = this
