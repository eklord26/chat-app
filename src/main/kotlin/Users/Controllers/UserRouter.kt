package Users.Controllers

import com.example.Users.Repository.UserRepository
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// TODO добавить шифрование данных синхронным ключём
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
    }
}
