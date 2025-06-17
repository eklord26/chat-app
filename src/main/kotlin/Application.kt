package com.example

import Base.Application.Configs
import Users.Controllers.UserRouting
import configureDatabases
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.contentnegotiation.*
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json() // This enables JSON serialization
    }
    configureSockets()
    configureSerialization()
    configureDatabases()
    configureMonitoring()
    configureRouting()
    UserRouting()
}

fun Application.getApplication(): Application = this
