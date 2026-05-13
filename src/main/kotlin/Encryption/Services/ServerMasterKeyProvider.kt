package Encryption.Services

import io.ktor.server.application.ApplicationEnvironment
import java.util.Base64

class ServerMasterKeyProvider(private val environment: ApplicationEnvironment? = null) {
    fun getKeyBase64(): String {
        val configuredKey = environment
            ?.config
            ?.propertyOrNull("encryption.masterKeyBase64")
            ?.getString()
            ?: System.getenv("CHAT_APP_MASTER_KEY_BASE64")

        if (!configuredKey.isNullOrBlank()) {
            validate(configuredKey)
            return configuredKey
        }

        val fallback = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        validate(fallback)
        return fallback
    }

    private fun validate(keyBase64: String) {
        require(Base64.getDecoder().decode(keyBase64).size == 32) {
            "Master encryption key must be a Base64 encoded 32-byte AES key"
        }
    }
}
