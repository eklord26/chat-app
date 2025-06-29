package com.example.Users.DTO

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val id: Int,
    var name: String,
    val login: String,
    val isAdmin: Boolean,
    val passwordHash: String,
    val deleted: Boolean)
