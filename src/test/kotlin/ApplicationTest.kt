package com.example

import Authentication.Controllers.AuthenticationRouter
import ChatMembers.DAO.ChatMembersTable
import Chats.Controllers.ChatRouting
import Chats.DAO.ChatTable
import Encryption.DAO.ChatEncryptionKeyTable
import Log.Controllers.LogRouter
import Logger.DAO.LogTable
import Registration.Controllers.RegistrationRouter
import Roles.DAO.RoleTable
import Tokens.DAO.TokenTable
import com.example.Users.DAO.UserTable
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApplicationTest {
    @Test
    fun registrationCreatesUserAndReturnsToken() = testApplication {
        application { testModule() }

        val response = client.post("/register") {
            jsonContent()
            setBody(registrationBody("registration_user"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.authToken())
    }

    @Test
    fun authenticationReturnsTokenForRegisteredUser() = testApplication {
        application { testModule() }
        client.register("auth_user")

        val response = client.post("/authentication") {
            jsonContent()
            setBody(
                """
                {
                    "login": "auth_user",
                    "password": "Password123!"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(response.authToken())
    }

    @Test
    fun logCreationRequiresTokenAndCreatesLog() = testApplication {
        application { testModule() }
        val token = client.register("log_user")

        val response = client.get("/logs/create") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun chatCreationUsesAuthenticatedUserAsOwner() = testApplication {
        application { testModule() }
        val token = client.register("chat_user")

        val response = client.post("/chats") {
            bearerAuth(token)
            jsonContent()
            setBody(
                """
                {
                    "name": "Private chat",
                    "owner": 999,
                    "createdAt": "2026-05-13T00:00:00Z"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertNotNull(Json.parseToJsonElement(response.bodyAsText()).jsonObject["id"]?.jsonPrimitive?.content)
    }

    private fun Application.testModule() {
        install(ContentNegotiation) {
            json()
        }
        configureTestDatabase()
        RegistrationRouter()
        AuthenticationRouter()
        LogRouter()
        ChatRouting()
    }

    private fun configureTestDatabase() {
        Database.connect(
            url = "jdbc:h2:mem:${UUID.randomUUID()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction {
            SchemaUtils.create(UserTable, TokenTable, RoleTable, ChatTable, ChatMembersTable, ChatEncryptionKeyTable, LogTable)
        }
    }

    private suspend fun io.ktor.client.HttpClient.register(login: String): String {
        val response = post("/register") {
            jsonContent()
            setBody(registrationBody(login))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        return response.authToken()
    }

    private suspend fun io.ktor.client.statement.HttpResponse.authToken(): String =
        Json.parseToJsonElement(bodyAsText()).jsonObject["authToken"]?.jsonPrimitive?.content
            ?: error("authToken was not returned")

    private fun registrationBody(login: String): String =
        """
        {
            "username": "$login",
            "fio": "Test User",
            "email": "$login@example.com",
            "phone": "+79991234567",
            "login": "$login",
            "password": "Password123!"
        }
        """.trimIndent()

    private fun io.ktor.client.request.HttpRequestBuilder.jsonContent() {
        header(io.ktor.http.HttpHeaders.ContentType, ContentType.Application.Json.toString())
    }
}
