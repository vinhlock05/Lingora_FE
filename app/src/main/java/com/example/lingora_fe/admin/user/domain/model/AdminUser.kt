package com.example.lingora_fe.admin.user.domain.model

data class AdminUser(
    val id: Int,
    val username: String,
    val email: String,
    val roles: List<UserRole>,
    val avatar: String?,
    val status: String,
    val proficiency: String? = null,
    val createdAt: String? = null
)

data class UserRole(
    val id: Int,
    val name: String
)

data class UserListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val users: List<AdminUser>
)

// Request data classes for domain layer
data class CreateUserData(
    val username: String,
    val email: String,
    val password: String,
    val roleIds: List<Int>,
    val proficiency: String
)

data class UpdateUserData(
    val username: String? = null,
    val email: String? = null,
    val newPassword: String? = null,
    val oldPassword: String? = null,
    val roleIds: List<Int>? = null,
    val proficiency: String? = null,
    val status: String? = null
)

// Filter data for queries
data class UserFilterOptions(
    val search: String? = null,
    val proficiency: String? = null,
    val status: String? = null,
    val sort: String? = null,
    val page: Int = 1,
    val limit: Int = 20
)

// Enums for type safety
enum class UserStatus(val value: String) {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    BANNED("BANNED"),
    DELETED("DELETED")
}

enum class ProficiencyLevel(val value: String) {
    BEGINNER("BEGINNER"),
    INTERMEDIATE("INTERMEDIATE"),
    ADVANCED("ADVANCED")
}

enum class UserRoleType(val value: String, val id: Int) {
    ADMIN("ADMIN", 1),
    LEARNER("LEARNER", 2)
}

enum class SortOption(val displayName: String, val apiValue: String) {
    ID_DESC("ID (Newest First)", "-id"),
    ID_ASC("ID (Oldest First)", "+id"),
    USERNAME_ASC("Username (A-Z)", "+username"),
    USERNAME_DESC("Username (Z-A)", "-username"),
    EMAIL_ASC("Email (A-Z)", "+email"),
    EMAIL_DESC("Email (Z-A)", "-email"),
    CREATED_AT_DESC("Created Date (Newest)", "-createdAt"),
    CREATED_AT_ASC("Created Date (Oldest)", "+createdAt")
}

