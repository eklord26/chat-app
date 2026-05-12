package db.migration

import Passwords.Builders.PasswordHashBuilder
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.nio.file.Files
import java.nio.file.Path
import java.security.SecureRandom

class V9__add_administrator_user : BaseJavaMigration() {
    override fun migrate(context: Context) {
        val password = generatePassword()
        val passwordHash = PasswordHashBuilder(password).build()

        val inserted = context.connection.prepareStatement(
            """
            INSERT INTO users (name, login, password_hash, is_admin)
            VALUES (?, ?, ?, TRUE)
            ON CONFLICT (login) DO NOTHING
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, "Administrator")
            statement.setString(2, "admin")
            statement.setString(3, passwordHash)
            statement.executeUpdate()
        }

        if (inserted > 0) {
            writeInitialPassword(password)
        }
    }

    private fun generatePassword(length: Int = 24): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*"
        val random = SecureRandom()

        return buildString(length) {
            repeat(length) {
                append(chars[random.nextInt(chars.length)])
            }
        }
    }

    private fun writeInitialPassword(password: String) {
        val configuredPath = System.getProperty("admin.initialPasswordFile")
            ?: System.getenv("ADMIN_INITIAL_PASSWORD_FILE")
            ?: "build/generated/admin-initial-password.txt"

        val path = Path.of(configuredPath)
        path.parent?.let(Files::createDirectories)
        Files.writeString(
            path,
            """
            login=admin
            password=$password
            """.trimIndent()
        )
    }
}
