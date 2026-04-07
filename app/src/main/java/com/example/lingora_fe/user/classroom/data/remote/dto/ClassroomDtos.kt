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
    @SerializedName("status") val status: String? = null,
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

data class ClassroomFlashcardDto(
    @SerializedName("id") val id: Int,
    @SerializedName("frontText") val frontText: String,
    @SerializedName("backText") val backText: String,
    @SerializedName("example") val example: String? = null,
    @SerializedName("audioUrl") val audioUrl: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null
)

data class CreateFlashcardRequest(
    @SerializedName("frontText") val frontText: String,
    @SerializedName("backText") val backText: String,
    @SerializedName("example") val example: String? = null,
    @SerializedName("audioUrl") val audioUrl: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null
)

data class UpdateFlashcardRequest(
    @SerializedName("frontText") val frontText: String? = null,
    @SerializedName("backText") val backText: String? = null,
    @SerializedName("example") val example: String? = null,
    @SerializedName("audioUrl") val audioUrl: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null
)

data class ClassroomLessonDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("lessonType") val lessonType: String,
    @SerializedName("content") val content: String? = null,
    @SerializedName("sortOrder") val sortOrder: Int? = null,
    @SerializedName("isPublished") val isPublished: Boolean? = null,
    @SerializedName("scheduledAt") val scheduledAt: String? = null,
    @SerializedName("flashcards") val flashcards: List<ClassroomFlashcardDto>? = null,
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
    @SerializedName("lessonId") val lessonId: Int? = null,
    @SerializedName("timeLimitSeconds") val timeLimitSeconds: Int? = null,
    @SerializedName("maxAttempts") val maxAttempts: Int,
    @SerializedName("passingScore") val passingScore: Double,
    @SerializedName("isPublished") val isPublished: Boolean,
    @SerializedName("opensAt") val opensAt: String? = null,
    @SerializedName("closesAt") val closesAt: String? = null,
    @SerializedName("questions") val questions: List<ClassroomQuizQuestionDto>? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class ClassroomQuizQuestionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("correctAnswer") val correctAnswer: String,
    @SerializedName("explanation") val explanation: String? = null
)

data class CreateQuizQuestionRequest(
    @SerializedName("type") val type: String,
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("correctAnswer") val correctAnswer: String,
    @SerializedName("explanation") val explanation: String? = null
)

data class UpdateQuizQuestionRequest(
    @SerializedName("type") val type: String? = null,
    @SerializedName("question") val question: String? = null,
    @SerializedName("options") val options: List<String>? = null,
    @SerializedName("correctAnswer") val correctAnswer: String? = null,
    @SerializedName("explanation") val explanation: String? = null
)

data class UpdateQuizRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("lessonId") val lessonId: Int? = null,
    @SerializedName("timeLimitSeconds") val timeLimitSeconds: Int? = null,
    @SerializedName("maxAttempts") val maxAttempts: Int? = null,
    @SerializedName("passingScore") val passingScore: Double? = null,
    @SerializedName("isPublished") val isPublished: Boolean? = null,
    @SerializedName("opensAt") val opensAt: String? = null,
    @SerializedName("closesAt") val closesAt: String? = null
)

data class ImportStudySetRequest(
    @SerializedName("studySetId") val studySetId: Int
)

data class CreateQuizRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("lessonId") val lessonId: Int? = null,
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
