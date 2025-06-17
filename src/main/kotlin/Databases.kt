import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    // Подключение к PostgreSQL (или H2 для тестов)
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/chatapp",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "postgres"
    )

    // Для H2 (вместо PostgreSQL):
    /*
    Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver",
        user = "sa",
        password = ""
    )
    */
}