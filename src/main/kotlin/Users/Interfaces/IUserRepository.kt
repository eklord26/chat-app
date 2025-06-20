package com.example.Users.Interfaces

import com.example.Users.DTO.User

interface IUserRepository {
    suspend fun findByLogin(login: String): User?

    suspend fun findByName(name: String): User?

    suspend fun findLikeName(name: String): User?
}