package com.example.Users.DAO

import com.example.Users.DTO.User
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UserTable : IntIdTable("users")
{
    val name = text("name")
    val login = varchar("login", 255)
    val emailCipherText = text("email_cipher_text").nullable()
    val emailNonce = text("email_nonce").nullable()
    val phoneCipherText = text("phone_cipher_text").nullable()
    val phoneNonce = text("phone_nonce").nullable()
    val fioCipherText = text("fio_cipher_text").nullable()
    val fioNonce = text("fio_nonce").nullable()
    val passwordHash = varchar("password_hash", 65)
    val isAdmin = bool("is_admin")
    val deletedAt = timestamp("deleted_at").nullable()
}

class UserDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDAO>(UserTable)

    var name by UserTable.name
    var login by UserTable.login
    var emailCipherText by UserTable.emailCipherText
    var emailNonce by UserTable.emailNonce
    var phoneCipherText by UserTable.phoneCipherText
    var phoneNonce by UserTable.phoneNonce
    var fioCipherText by UserTable.fioCipherText
    var fioNonce by UserTable.fioNonce
    var passwordHash by UserTable.passwordHash
    var isAdmin by UserTable.isAdmin
    var deletedAt by UserTable.deletedAt
}

fun daoToModel(dao: UserDAO?): User? = dao?.let {
    User(
        it.id.value,
        it.name,
        it.login,
        null,
        null,
        null,
        it.isAdmin,
        it.passwordHash,
        it.deletedAt.toString()
    )
}
