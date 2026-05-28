import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            val index = checkNotNull(
                Thread.currentThread().contextClassLoader.getResource("static/index.html")
            ) { "static/index.html was not found" }
            call.respondBytes(index.readBytes(), ContentType.Text.Html)
        }
        get("/test") {
        }
        get("/live")
        {
            call.respond(true)
        }

        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}
