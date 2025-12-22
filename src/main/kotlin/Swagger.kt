import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.config.OutputFormat
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.config.OperationsSort
import io.github.smiley4.ktorswaggerui.config.SwaggerUISyntaxHighlight
import io.github.smiley4.ktorswaggerui.config.TagSort
import io.github.smiley4.ktorswaggerui.swaggerUI

fun Application.configureSwagger() {
    install(OpenApi) {
        info {
            title = "My Messenger API"
            version = "1.0.0"
            description = "API для чатов, пользователей и сообщений"
        }
        security {
            securityScheme("MySecurityScheme") {
                type = AuthType.HTTP
                scheme = AuthScheme.BASIC
            }
            defaultSecuritySchemeNames("MySecurityScheme")
            defaultUnauthorizedResponse {
                description = "Username or password is invalid"
            }
        }
        tags {
            tag("users") {
                description = "Работа с пользователями"
                externalDocUrl = "example.com"
                externalDocDescription = "Users documentation"
            }
            tag("messages") {
                description = "Работа с сообщениями"
                externalDocUrl = "example.com"
                externalDocDescription = "Messages documentation"
            }
            tag("chats") {
                description = "Работа с чатами"
                externalDocUrl = "example.com"
                externalDocDescription = "Chats documentation"
            }
            tag("chat_members") {
                description = "Работа с участниками чатов"
                externalDocUrl = "example.com"
                externalDocDescription = "Chat Members documentation"
            }
            tag("media") {
                description = "Работа с Медиа"
                externalDocUrl = "example.com"
                externalDocDescription = "Media documentation"
            }
        }
        outputFormat = OutputFormat.JSON

    }

    // 2. Настраиваем маршруты для отображения
    routing {
        // Эндпоинт, отдающий сам JSON-файл спецификации
        route("openapi.json") {
            openApi()
        }

        // Эндпоинт, отображающий Swagger UI
        route("swagger-ui") {
            swaggerUI("/openapi.json") {
                displayOperationId = false
                operationsSorter = OperationsSort.HTTP_METHOD
                syntaxHighlight = SwaggerUISyntaxHighlight.OBSIDIAN
                tryItOutEnabled = true
                tagsSorter = TagSort.NONE
                showCommonExtensions = true
                displayRequestDuration = true
            }
        }
    }
}