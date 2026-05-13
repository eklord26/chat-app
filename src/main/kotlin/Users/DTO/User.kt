package com.example.Users.DTO

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int? = null,
    var name: String,
    val login: String,
    val email: String? = null,
    val phone: String? = null,
    val fio: String? = null,
    val isAdmin: Boolean,
    val passwordHash: String,
    val deletedAt: String? = null
)
