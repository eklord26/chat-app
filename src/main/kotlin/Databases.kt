import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val urlProperty = environment.config.propertyOrNull("postgres.url")
    if (urlProperty == null) {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )
        return
    }

    val url = urlProperty.getString()
    val user = environment.config.property("postgres.user").getString()
    val password = environment.config.property("postgres.password").getString()

    Flyway.configure()
        .dataSource(url, user, password)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .validateOnMigrate(true)
        .load()
        .migrate()

    Database.connect(
        url = url,
        driver = "org.postgresql.Driver",
        user = user,
        password = password
    )
}
