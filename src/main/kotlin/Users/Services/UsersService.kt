package com.example.Users.Services

import com.example.Users.DTO.User
import com.example.Users.Repository.UserRepository

class UsersService {
    private val userRepository = UserRepository()

    public suspend fun Registration(user: User): Boolean
    {
        if (userRepository.findByLogin(user.login) == null)
        {
            userRepository.create(user)
            return true
        } else return false
    }

    public suspend fun Authorization(login: String, passwordHash: String): Boolean
    {
        val user = userRepository.findByLogin(login)
        return (user.first()?.passwordHash == passwordHash)
    }

    public suspend fun changeName(newName: String, id: Int): Boolean
    {
        val user = userRepository.findById(id)
        if (user != null)
        {
            userRepository.updateById(id, user)
        }

        return true
    }

    public suspend fun changeLogin(newLogin: String, id: Int): Boolean
    {

        return true
    }

    public suspend fun changePassword(newPassword: String, id: Int): Boolean
    {
        return true
    }

    public suspend fun deleteUser(id: Int): Boolean
    {
        return true
    }
}