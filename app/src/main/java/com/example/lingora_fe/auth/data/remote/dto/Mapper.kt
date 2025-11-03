package com.example.lingora_fe.auth.data.remote.dto

import com.example.lingora_fe.auth.domain.model.Role
import com.example.lingora_fe.auth.domain.model.User

fun UserDto.toDomainModel(): User {
    return User(
        id = this.id,
        username = this.username,
        email = this.email,
        roles = this.roles.map { it.toDomainModel() },
        avatar = this.avatar,
        status = this.status,
        proficiency = this.proficiency,
        createdAt = this.createdAt
    )
}

fun RoleDto.toDomainModel(): Role {
    return Role(
        id = this.id,
        name = this.name
    )
}

