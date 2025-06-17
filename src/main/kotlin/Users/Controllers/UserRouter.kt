package Users.Controllers

import com.example.Users.Repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.UserRouting() {
    routing {
        get("/users")
        {
            val users = UserRepository().findAll()
            call.respond(users)
        }
        get("/users/id")
        {
            val id = call.request.queryParameters["id"]
            if (id !== null) {
                val user = UserRepository().findById(id.toInt())
                if (user !== null) {
                    call.respond(user)
                }
            }
        }
        get("/users/login")
        {
            val test = call.request.header(HttpHeaders.XRequestId)
            val login = call.request.queryParameters["login"]
            if (login !== null) {
                val user = UserRepository().findByLogin(login)
                if (user !== null) {
                    call.respond(user)
                }
            }
        }
    }
}
