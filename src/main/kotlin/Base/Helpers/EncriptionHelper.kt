package Base.Helpers

import Base.Interfaces.IEncryptionHelper
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncriptionHelper: IEncryptionHelper {

    private val alg = "AES"
    private val transform = "AES/CBC/PKCS5Padding"
    private val size = 32

    public override fun code(data: String, key: String): String {
        val iv = ByteArray(size)

        Random().nextBytes(iv)

        val ivSpec = IvParameterSpec(iv)
        val secretKey = generateKey(key)
        val cipher = Cipher.getInstance(transform)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

        val encryptedData = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        val combined = ByteArray(iv.size + encryptedData.size)

        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedData, 0, combined, iv.size, encryptedData.size)

        return Base64.getEncoder().encodeToString(combined)
    }

    public override fun decode(data: String, key: String): String {
        val combined = Base64.getDecoder().decode(data)
        val iv = ByteArray(size)

        System.arraycopy(combined, 0, iv, 0, iv.size)

        val ivSpec = IvParameterSpec(iv)
        val encryptedBytes = ByteArray(combined.size - iv.size)

        System.arraycopy(combined, iv.size, encryptedBytes, 0, encryptedBytes.size)

        val secretKey = generateKey(key)
        val cipher = Cipher.getInstance(transform)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    private fun generateKey(keyStr: String): SecretKey {
        val keyBytes = keyStr.toByteArray(Charsets.UTF_8)
        return SecretKeySpec(keyBytes, alg)
    }

    fun generateRandomString(size: Int = 32): String {
        val allowedChars = (
                ('A'..'Z') + ('a'..'z') +
                        ('0'..'9') +
                        "!@#$%^&*()_+-=[]{}|;:,.<>?/~`"
                ).toList()

        val result = StringBuilder()
        var currentBytes = 0

        while (currentBytes < size) {
            val randomChar = allowedChars.random()
            val charBytes = randomChar.toString().toByteArray(Charsets.UTF_8).size

            when {
                currentBytes + charBytes <= size -> {
                    result.append(randomChar)
                    currentBytes += charBytes
                }
                size - currentBytes == 1 -> {
                    // Добавляем только ASCII символ (ровно 1 байт)
                    result.append((32..126).random().toChar()) // ASCII printable chars
                    currentBytes += 1
                    break
                }
                else -> break
            }
        }

        return result.toString()
    }
}