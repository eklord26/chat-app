package com.example.Users.Services

import Users.DTO.UserFilter
import com.example.Users.DTO.User
import com.example.Users.Repository.UserRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserService {
    private val userRepository = UserRepository()

    suspend fun changeName(newName: String, id: Int): Boolean {
        var user = userRepository.findById(id)
        if (user != null) {
            user = user.copy(name = newName)
            userRepository.updateById(id, user)
            return true
        }
        return false
    }

    suspend fun changePassword(newPassword: String, id: Int): Boolean {
        var user = userRepository.findById(id)
        if (user != null) {
            user = user.copy(passwordHash = newPassword)
            userRepository.updateById(id, user)
            return true
        }
        return false
    }

    suspend fun create(user: User): Int? {
        if (!checkLogin(user.login)) {
            userRepository.create(user)
            val filter = UserFilter(login = user.login)
            return userRepository.findByFilter(filter).firstOrNull()?.id
        }
        return null
    }

    suspend fun update(id: Int, user: User): Boolean {
        if (findById(id) != null) {
            userRepository.updateById(id, user)
            return true
        }
        return false
    }

    suspend fun delete(id: Int): Boolean {
        val user = findById(id)
        if (user != null) {
            val deletedUser = user.copy(
                deletedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            userRepository.updateById(id, deletedUser)
            return true
        }
        return false
    }

    suspend fun checkLogin(login: String): Boolean {
        val filter = UserFilter(login = login)
        return userRepository.findByFilter(filter).firstOrNull() != null
    }

    suspend fun findByFilter(filter: UserFilter): List<User?> {
        return userRepository.findByFilter(filter)
    }

    suspend fun findById(id: Int): User? {
        return userRepository.findById(id)
    }
}