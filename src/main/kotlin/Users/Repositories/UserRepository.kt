package com.example.Users.Repository

import Base.Interfaces.IBaseRepository
import Encryption.DTO.EncryptedPayload
import Encryption.Services.UserPersonalDataEncryptionService
import Users.DTO.UserFilter
import Users.Validators.UserContactValidator
import com.example.Base.Helpers.suspendTransaction
import com.example.Users.DAO.UserDAO
import com.example.Users.DAO.UserTable
import com.example.Users.DTO.User
import io.ktor.server.application.ApplicationEnvironment
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and

class UserRepository(environment: ApplicationEnvironment? = null): IBaseRepository<User, UserFilter> {
    private val personalDataEncryptionService = UserPersonalDataEncryptionService(environment)

    override suspend fun findById(id: Int): User? = suspendTransaction {
        daoToModel(UserDAO.findById(id))
    }

    override suspend fun findByFilter(filter: UserFilter): List<User?> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        filter.login?.let { conditions.add(UserTable.login eq it) }
        filter.name?.let { conditions.add(UserTable.name like "%$it%") }
        filter.isAdmin?.let { conditions.add(UserTable.isAdmin eq it) }

        filter.isDeleted?.let { isDeleted ->
            if (isDeleted) conditions.add(UserTable.deletedAt.isNotNull())
            else conditions.add(UserTable.deletedAt.isNull())
        }

        if (conditions.isEmpty()) {
            UserDAO.all().map { daoToModel(it) }
        } else {
            val finalOp = conditions.reduce { acc, op -> acc and op }
            UserDAO.find(finalOp).map { daoToModel(it) }
        }
    }

    override suspend fun findAll(): List<User?> = suspendTransaction {
        UserDAO.all().map { daoToModel(it) }
    }

    override suspend fun create(entity: User): Unit = suspendTransaction {
        validatePersonalData(entity)
        val email = entity.email?.let { personalDataEncryptionService.encrypt(it.trim()) }
        val phone = entity.phone?.let { personalDataEncryptionService.encrypt(UserContactValidator.normalizePhone(it)) }
        val fio = entity.fio?.let { personalDataEncryptionService.encrypt(it.trim()) }

        UserDAO.new {
            name = entity.name
            login = entity.login
            emailCipherText = email?.cipherText
            emailNonce = email?.nonce
            phoneCipherText = phone?.cipherText
            phoneNonce = phone?.nonce
            fioCipherText = fio?.cipherText
            fioNonce = fio?.nonce
            passwordHash = entity.passwordHash
            isAdmin = entity.isAdmin
            deletedAt = entity.deletedAt?.let { java.time.Instant.parse(it) }
        }
    }

    override suspend fun updateById(id: Int, entity: User): Unit = suspendTransaction {
        validatePersonalData(entity)
        val email = entity.email?.let { personalDataEncryptionService.encrypt(it.trim()) }
        val phone = entity.phone?.let { personalDataEncryptionService.encrypt(UserContactValidator.normalizePhone(it)) }
        val fio = entity.fio?.let { personalDataEncryptionService.encrypt(it.trim()) }

        UserDAO.findByIdAndUpdate(id) {
            it.name = entity.name
            it.login = entity.login
            it.emailCipherText = email?.cipherText
            it.emailNonce = email?.nonce
            it.phoneCipherText = phone?.cipherText
            it.phoneNonce = phone?.nonce
            it.fioCipherText = fio?.cipherText
            it.fioNonce = fio?.nonce
            it.passwordHash = entity.passwordHash
            it.isAdmin = entity.isAdmin
            it.deletedAt = entity.deletedAt?.let { date -> java.time.Instant.parse(date) }
        }
    }

    private fun daoToModel(dao: UserDAO?): User? = dao?.let {
        User(
            id = it.id.value,
            name = it.name,
            login = it.login,
            email = decryptNullable(it.emailCipherText, it.emailNonce),
            phone = decryptNullable(it.phoneCipherText, it.phoneNonce),
            fio = decryptNullable(it.fioCipherText, it.fioNonce),
            isAdmin = it.isAdmin,
            passwordHash = it.passwordHash,
            deletedAt = it.deletedAt?.toString()
        )
    }

    private fun decryptNullable(cipherText: String?, nonce: String?): String? {
        if (cipherText == null || nonce == null) return null
        return personalDataEncryptionService.decrypt(
            EncryptedPayload(
                cipherText = cipherText,
                nonce = nonce,
                keyVersion = 1
            )
        )
    }

    private fun validatePersonalData(user: User) {
        user.email?.let {
            require(UserContactValidator.isValidEmail(it)) { "Invalid email format" }
        }
        user.phone?.let {
            require(UserContactValidator.isValidPhone(it)) { "Invalid phone format" }
        }
    }
}
