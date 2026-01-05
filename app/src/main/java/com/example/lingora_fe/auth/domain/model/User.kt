package com.example.lingora_fe.auth.domain.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val roles: List<Role>,
    val avatar: String?,
    val status: String,
    val proficiency: String? = null,
    val createdAt: String? = null,
    val deletedAt: String? = null,
    val hasPassword: Boolean = false,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: String? = null
)

data class Role(
    val id: Int,
    val name: String
)

