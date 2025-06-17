package com.example.Users.DAO

import com.example.Users.DTO.User
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("users")
{
    val name = text("name")
    val login = varchar("login", 255)
    val passwordHash = varchar("password_hash", 65)
    val isAdmin = bool("is_admin")
    val deleted = bool("deleted")
}

class UserDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDAO>(UserTable)

    var name by UserTable.name
    var login by UserTable.login
    var passwordHash by UserTable.passwordHash
    var isAdmin by UserTable.isAdmin
    var deleted by UserTable.deleted
}

fun daoToModel(dao: UserDAO?): User? = dao?.let {
    User(
        it.id.value,
        it.name,
        it.login,
        it.isAdmin,
        it.passwordHash,
        it.deleted
    )
}