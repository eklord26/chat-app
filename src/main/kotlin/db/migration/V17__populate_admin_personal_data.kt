package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class V17__populate_admin_personal_data : BaseJavaMigration() {
    private val random = SecureRandom()

    override fun migrate(context: Context) {
        val masterKey = getMasterKeyBase64()
        val email = encrypt("admin@example.local", masterKey)
        val phone = encrypt("+70000000000", masterKey)
        val fio = encrypt("Administrator", masterKey)

        context.connection.prepareStatement(
            """
            UPDATE users
            SET email_cipher_text = COALESCE(email_cipher_text, ?),
                email_nonce = COALESCE(email_nonce, ?),
                phone_cipher_text = COALESCE(phone_cipher_text, ?),
                phone_nonce = COALESCE(phone_nonce, ?),
                fio_cipher_text = COALESCE(fio_cipher_text, ?),
                fio_nonce = COALESCE(fio_nonce, ?)
            WHERE login = 'admin'
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, email.cipherText)
            statement.setString(2, email.nonce)
            statement.setString(3, phone.cipherText)
            statement.setString(4, phone.nonce)
            statement.setString(5, fio.cipherText)
            statement.setString(6, fio.nonce)
            statement.executeUpdate()
        }
    }

    private fun encrypt(value: String, keyBase64: String): EncryptedMigrationValue {
        val nonce = ByteArray(NONCE_SIZE_BYTES)
        random.nextBytes(nonce)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(Base64.getDecoder().decode(keyBase64), "AES"),
            GCMParameterSpec(TAG_SIZE_BITS, nonce)
        )

        return EncryptedMigrationValue(
            cipherText = Base64.getEncoder().encodeToString(cipher.doFinal(value.toByteArray(Charsets.UTF_8))),
            nonce = Base64.getEncoder().encodeToString(nonce)
        )
    }

    private fun getMasterKeyBase64(): String {
        val configuredKey = System.getProperty("encryption.masterKeyBase64")
            ?: System.getenv("CHAT_APP_MASTER_KEY_BASE64")
            ?: DEFAULT_MASTER_KEY_BASE64

        require(Base64.getDecoder().decode(configuredKey).size == KEY_SIZE_BYTES) {
            "Master encryption key must be a Base64 encoded 32-byte AES key"
        }

        return configuredKey
    }

    private data class EncryptedMigrationValue(
        val cipherText: String,
        val nonce: String
    )

    private companion object {
        const val KEY_SIZE_BYTES = 32
        const val NONCE_SIZE_BYTES = 12
        const val TAG_SIZE_BITS = 128
        const val DEFAULT_MASTER_KEY_BASE64 = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
    }
}
