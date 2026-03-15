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
    val passwordHash = varchar("password_hash", 65)
    val isAdmin = bool("is_admin")
    val deletedAt = timestamp("deletedAt")
}

class UserDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDAO>(UserTable)

    var name by UserTable.name
    var login by UserTable.login
    var passwordHash by UserTable.passwordHash
    var isAdmin by UserTable.isAdmin
    var deletedAt by UserTable.deletedAt
}

fun daoToModel(dao: UserDAO?): User? = dao?.let {
    User(
        it.id.value,
        it.name,
        it.login,
        it.isAdmin,
        it.passwordHash,
        it.deletedAt.toString()
    )
}