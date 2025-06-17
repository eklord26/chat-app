package com.example.Users.Interfaces

import com.example.Users.DTO.User

interface IUserRepository {
    suspend fun findByLogin(login: String): User?
}