package com.example

import Authentication.Controllers.AuthenticationRouter
import ChatMembers.Controllers.ChatMemberRouting
import Chats.Controllers.ChatRouting
import Contacts.Controllers.ContactRouting
import Encryption.Controllers.EncryptionRouting
import Invitations.Controllers.ChatInvitationRouting
import Invitations.Controllers.ContactInvitationRouting
import Log.Controllers.LogRouter
import Messages.Controllers.MessageRouting
import Registration.Controllers.RegistrationRouter
import Roles.Controllers.RoleRouting
import Rights.Controllers.RightRouting
import Users.Controllers.UserRouting
import Web.Endpoints.WebEndpointRouting
import configureDatabases
import configureRouting
import configureSwagger
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
    ChatMemberRouting()
    ContactRouting()
    ContactInvitationRouting()
    ChatInvitationRouting()
    WebEndpointRouting()
    RoleRouting()
    MessageRouting()
    ChatRouting()
    EncryptionRouting()
    UserRouting()
    RightRouting()
    LogRouter()
    RegistrationRouter()
    AuthenticationRouter()
    configureSwagger()
}

fun Application.getApplication(): Application = this
