package com.example.Users.Services

import com.example.Users.Repository.UserRepository

class UsersService {
    private val userRepository = UserRepository()

    public suspend fun changeName(newName: String, id: Int): Boolean {
        val user = userRepository.findById(id)

        if (user != null) {
            user.copy(name = newName)
            userRepository.updateById(id, user)
        } else return false
        return true
    }

    public suspend fun changePassword(newPassword: String, id: Int): Boolean
    {
        val user = userRepository.findById(id)

        if (user != null) {
            user.copy(passwordHash = newPassword)
            userRepository.updateById(id, user)
        } else return false
        return true
    }

    public suspend fun deleteUser(id: Int): Boolean
    {
        val user = userRepository.findById(id)
        if (user != null) {
            user.copy(deleted = true)
            userRepository.updateById(id, user)
        } else return false
        return true
    }

    public suspend fun checkLogin(login: String): Boolean {
        return (userRepository.findByLogin(login) !== null)
    }

    public suspend fun checkDeletedByLogin(login: String): Boolean {
        return (userRepository.findByLogin(login)?.deleted !== true)
    }
}