package com.example.lingora_fe.auth.domain.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val roles: List<Role>,
    val avatar: String?,
    val status: String,
    val proficiency: String,
    val createdAt: String? = null,
    val deletedAt: String? = null
)

data class Role(
    val id: Int,
    val name: String
)

