package com.example.lingora_fe.admin.user.data.remote.dto

import com.example.lingora_fe.admin.user.domain.model.AdminUser
import com.example.lingora_fe.admin.user.domain.model.CreateUserData
import com.example.lingora_fe.admin.user.domain.model.UpdateUserData
import com.example.lingora_fe.admin.user.domain.model.UserRole
import com.example.lingora_fe.admin.user.domain.model.UserListMetadata

// DTO to Domain mappers
fun AdminUserDto.toDomain(): AdminUser {
    return AdminUser(
        id = id,
        username = username,
        email = email,
        roles = roles.map { it.toDomain() },
        avatar = avatar,
        status = status,
        proficiency = proficiency,
        createdAt = createdAt
    )
}

fun UserRoleDto.toDomain(): UserRole {
    return UserRole(
        id = id,
        name = name
    )
}

fun UserListMetaData.toDomain(): UserListMetadata {
    return UserListMetadata(
        currentPage = currentPage,
        totalPages = totalPages,
        total = total,
        users = users.map { it.toDomain() }
    )
}

// Domain to DTO mappers (for requests)
fun CreateUserData.toDto(): CreateUserRequest {
    return CreateUserRequest(
        username = username,
        email = email,
        password = password,
        roleIds = roleIds,
        proficiency = proficiency
    )
}

fun UpdateUserData.toDto(): UpdateUserRequest {
    return UpdateUserRequest(
        username = username,
        email = email,
        newPassword = newPassword,
        oldPassword = oldPassword,
        roleIds = roleIds,
        proficiency = proficiency,
        status = status,
        avatar = avatar
    )
}

