package Encryption.Services

import ChatMembers.DTO.ChatMemberFilter
import ChatMembers.Repositories.ChatMemberRepository
import Encryption.DTO.ChatEncryptionKey
import Encryption.DTO.ChatEncryptionKeyResponse
import Encryption.DTO.ChatEncryptionKeysResponse
import Encryption.DTO.EncryptedPayload
import Encryption.Repositories.ChatEncryptionKeyRepository
import io.ktor.server.application.ApplicationEnvironment
import java.time.Instant

class ChatEncryptionKeyService(
    environment: ApplicationEnvironment? = null,
    private val repository: ChatEncryptionKeyRepository = ChatEncryptionKeyRepository(),
    private val memberRepository: ChatMemberRepository = ChatMemberRepository(),
    private val encryptionService: AesGcmEncryptionService = AesGcmEncryptionService()
) {
    private val masterKey = ServerMasterKeyProvider(environment).getKeyBase64()

    suspend fun getOrCreateForUser(idChat: Int, idUser: Int): ChatEncryptionKeyResponse? {
        if (!isActiveChatMember(idChat, idUser)) return null
        return repository.findActiveByChat(idChat)?.toResponse()
            ?: createKey(idChat, version = 1).toResponse()
    }

    suspend fun findByVersionForUser(idChat: Int, idUser: Int, version: Int): ChatEncryptionKeyResponse? {
        val member = findMembership(idChat, idUser) ?: return null
        val key = repository.findByChatAndVersion(idChat, version) ?: return null
        if (!canAccessKey(member.createdAt, member.deletedAt, key.createdAt)) return null
        return key.toResponse()
    }

    suspend fun findAccessibleKeysForUser(idChat: Int, idUser: Int): ChatEncryptionKeysResponse? {
        val member = findMembership(idChat, idUser) ?: return null
        val keys = repository.findAllByChat(idChat)
            .filter { key -> canAccessKey(member.createdAt, member.deletedAt, key.createdAt) }
            .map { key -> key.toResponse() }

        return ChatEncryptionKeysResponse(idChat = idChat, keys = keys)
    }

    suspend fun rotateForUser(idChat: Int, idUser: Int): ChatEncryptionKeyResponse? {
        if (!isActiveChatMember(idChat, idUser)) return null

        val previousVersion = repository.findActiveByChat(idChat)?.version ?: 0
        repository.revokeActive(idChat)
        return createKey(idChat, previousVersion + 1).toResponse()
    }

    private suspend fun isActiveChatMember(idChat: Int, idUser: Int): Boolean =
        memberRepository.findByFilter(ChatMemberFilter(idChat = idChat, idUser = idUser, isDeleted = false)).isNotEmpty()

    private suspend fun findMembership(idChat: Int, idUser: Int) =
        memberRepository.findByFilter(ChatMemberFilter(idChat = idChat, idUser = idUser))
            .filterNotNull()
            .maxByOrNull { member -> Instant.parse(member.createdAt) }

    private fun canAccessKey(memberCreatedAt: String, memberDeletedAt: String?, keyCreatedAt: String): Boolean {
        val joinedAt = Instant.parse(memberCreatedAt)
        val leftAt = memberDeletedAt?.let(Instant::parse)
        val keyStartedAt = Instant.parse(keyCreatedAt)

        return !keyStartedAt.isBefore(joinedAt) && (leftAt == null || keyStartedAt.isBefore(leftAt))
    }

    private suspend fun createKey(idChat: Int, version: Int): ChatEncryptionKey {
        val chatKey = encryptionService.generateKeyBase64()
        val encrypted = encryptionService.encrypt(chatKey, masterKey, version)
        return repository.create(
            ChatEncryptionKey(
                idChat = idChat,
                keyCipherText = encrypted.cipherText,
                nonce = encrypted.nonce,
                algorithm = encrypted.algorithm,
                version = version,
                createdAt = ""
            )
        ) ?: error("Unable to create chat encryption key")
    }

    private fun ChatEncryptionKey.toResponse(): ChatEncryptionKeyResponse {
        val key = encryptionService.decrypt(
            EncryptedPayload(
                cipherText = keyCipherText,
                nonce = nonce,
                algorithm = algorithm,
                keyVersion = version
            ),
            masterKey
        )
        return ChatEncryptionKeyResponse(idChat, key, algorithm, version, createdAt, rotatedAt)
    }
}
