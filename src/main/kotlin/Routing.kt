import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondRedirect("/static/index.html")
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
