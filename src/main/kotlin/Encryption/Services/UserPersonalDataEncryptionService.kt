package Encryption.Services

import Encryption.DTO.EncryptedPayload
import io.ktor.server.application.ApplicationEnvironment

class UserPersonalDataEncryptionService(
    environment: ApplicationEnvironment? = null,
    private val encryptionService: AesGcmEncryptionService = AesGcmEncryptionService()
) {
    private val masterKey = ServerMasterKeyProvider(environment).getKeyBase64()

    fun encrypt(value: String): EncryptedPayload = encryptionService.encrypt(value, masterKey)

    fun decrypt(payload: EncryptedPayload): String = encryptionService.decrypt(payload, masterKey)
}
