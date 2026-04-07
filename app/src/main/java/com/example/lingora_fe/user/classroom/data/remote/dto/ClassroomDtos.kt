package com.example.lingora_fe.user.classroom.data.remote.dto

import com.google.gson.annotations.SerializedName

// ─── Shared User DTO ──────────────────────────────────────────────────────────

data class ClassroomUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String? = null,
    @SerializedName("avatar") val avatar: String? = null
)

// ─── Classroom ────────────────────────────────────────────────────────────────

data class ClassroomDto(
    @SerializedName("id") val id: Int,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("coverImageUrl") val coverImageUrl: String? = null,
    @SerializedName("maxStudents") val maxStudents: Int,
    @SerializedName("status") val status: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("settings") val settings: Map<String, Any>? = null,
    @SerializedName("teacher") val teacher: ClassroomUserDto? = null,
    @SerializedName("totalMembers") val totalMembers: Int? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class ClassroomListMetaData(
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("classrooms") val classrooms: List<ClassroomDto>
)

data class CreateClassroomRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("coverImageUrl") val coverImageUrl: String? = null,
    @SerializedName("maxStudents") val maxStudents: Int? = null,
    @SerializedName("isPublic") val isPublic: Boolean? = null,
    @SerializedName("settings") val settings: Map<String, Any>? = null
)

data class UpdateClassroomRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("coverImageUrl") val coverImageUrl: String? = null,
    @SerializedName("maxStudents") val maxStudents: Int? = null,
    @SerializedName("isPublic") val isPublic: Boolean? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("settings") val settings: Map<String, Any>? = null
)

// ─── Lesson ───────────────────────────────────────────────────────────────────

data class ClassroomLessonDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("lessonType") val lessonType: String,
    @SerializedName("content") val content: String? = null,
    @SerializedName("sortOrder") val sortOrder: Int? = null,
    @SerializedName("isPublished") val isPublished: Boolean? = null,
    @SerializedName("scheduledAt") val scheduledAt: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class CreateLessonRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("lessonType") val lessonType: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("sortOrder") val sortOrder: Int? = null
)

data class UpdateLessonRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("lessonType") val lessonType: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("sortOrder") val sortOrder: Int? = null,
    @SerializedName("isPublished") val isPublished: Boolean? = null,
    @SerializedName("scheduledAt") val scheduledAt: String? = null
)

// ─── Member ───────────────────────────────────────────────────────────────────

data class ClassroomMemberDto(
    @SerializedName("id") val id: Int,
    @SerializedName("user") val user: ClassroomUserDto,
    @SerializedName("role") val role: String,
    @SerializedName("status") val status: String,
    @SerializedName("joinedAt") val joinedAt: String? = null,
    @SerializedName("removedAt") val removedAt: String? = null
)

// ─── Quiz ─────────────────────────────────────────────────────────────────────

data class ClassroomQuizDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("timeLimitSeconds") val timeLimitSeconds: Int? = null,
    @SerializedName("maxAttempts") val maxAttempts: Int,
    @SerializedName("passingScore") val passingScore: Double,
    @SerializedName("isPublished") val isPublished: Boolean,
    @SerializedName("opensAt") val opensAt: String? = null,
    @SerializedName("closesAt") val closesAt: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class CreateQuizRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("timeLimitSeconds") val timeLimitSeconds: Int? = null,
    @SerializedName("maxAttempts") val maxAttempts: Int? = null,
    @SerializedName("passingScore") val passingScore: Double? = null,
    @SerializedName("opensAt") val opensAt: String? = null,
    @SerializedName("closesAt") val closesAt: String? = null
)

// ─── Chat Message ─────────────────────────────────────────────────────────────

data class ClassroomMessageDto(
    @SerializedName("id") val id: Int,
    @SerializedName("sender") val sender: ClassroomUserDto,
    @SerializedName("type") val type: String,
    @SerializedName("content") val content: String,
    @SerializedName("attachmentUrl") val attachmentUrl: String? = null,
    @SerializedName("repliedTo") val repliedTo: ClassroomMessageDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

// ─── Join by Code ─────────────────────────────────────────────────────────────

data class JoinClassroomRequest(
    @SerializedName("code") val code: String
)
