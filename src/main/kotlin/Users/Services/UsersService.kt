package com.example.Users.Services

import com.example.Users.DTO.User
import com.example.Users.Repository.UserRepository

class UsersService {
    private val userRepository = UserRepository()

    public suspend fun changeName(newName: String, id: Int): Boolean {
        var user = userRepository.findById(id)

        if (user !== null) {
            user = user.copy(name = newName)
            userRepository.updateById(id, user)
        } else return false
        return true
    }

    public suspend fun changePassword(newPassword: String, id: Int): Boolean
    {
        var user = userRepository.findById(id)

        if (user !== null) {
            user = user.copy(passwordHash = newPassword)
            userRepository.updateById(id, user)
        } else return false
        return true
    }

    public suspend fun createUser(user: User): Int?
    {
        if (!this.checkLogin(user.login)) {
            userRepository.create(user)
            return userRepository.findByLogin(user.login)?.id!!
        } else return null
    }

    public suspend fun updateUser(user: User): Boolean
    {
        if (this.checkLogin(user.login) && (this.checkDeletedByLogin(user.login) == false)) {
            userRepository.updateById(user.id, user)
            return true
        } else return false
    }

    public suspend fun deleteUser(id: Int): Boolean
    {
        val user = userRepository.findById(id)
        if (user !== null) {
            user.copy(deleted = true)
            userRepository.updateById(id, user)
            return true
        } else return false
    }

    public suspend fun checkLogin(login: String): Boolean {
        return (userRepository.findByLogin(login) !== null)
    }

    public suspend fun checkDeletedByLogin(login: String): Boolean? {
        val user: User? = userRepository.findByLogin(login)
        return if (user !== null) {
            user.deleted
        } else null
    }
}