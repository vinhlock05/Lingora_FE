package com.example.lingora_fe.user.studyset.domain.model

data class StudySet(
    val id: Int,
    val title: String,
    val description: String?,
    val visibility: StudySetVisibility,
    val price: Int,
    val status: StudySetStatus,
    val likeCount: Int,
    val createdAt: String,
    val updatedAt: String?,
    val owner: Owner,
    val flashcards: List<Flashcard> = emptyList(),
    val quizzes: List<Quiz> = emptyList(),
    val totalFlashcards: Int? = null,
    val totalQuizzes: Int? = null,
    val isPurchased: Boolean? = null,
    val isAlreadyLike: Boolean = false
)

data class Owner(
    val id: Int,
    val username: String
)

data class Flashcard(
    val id: Int? = null,
    val frontText: String,
    val backText: String,
    val example: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null
)

data class Quiz(
    val id: Int? = null,
    val type: QuizType,
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)

data class StudySetListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val studySets: List<StudySet>
)

// Request data classes for domain layer
data class CreateStudySetData(
    val title: String,
    val description: String? = null,
    val visibility: StudySetVisibility = StudySetVisibility.PRIVATE,
    val price: Int = 0,
    val flashcards: List<Flashcard> = emptyList(),
    val quizzes: List<Quiz> = emptyList()
)

data class UpdateStudySetData(
    val title: String? = null,
    val description: String? = null,
    val visibility: StudySetVisibility? = null,
    val price: Int? = null,
    val status: StudySetStatus? = null,
    val flashcards: List<Flashcard>? = null,
    val quizzes: List<Quiz>? = null
)

data class BuyStudySetResponse(
    val paymentUrl: String?,
    val isFree: Boolean,
    val message: String? = null,
    val orderId: String? = null,
    val amount: Int? = null,
    val transactionId: Int? = null
)

// Filter data for queries
data class StudySetFilterOptions(
    val search: String? = null,
    val visibility: StudySetVisibility? = null,
    val status: StudySetStatus? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val sort: String? = null,
    val page: Int = 1,
    val limit: Int = 10
)

// Enums for type safety
enum class StudySetStatus(val value: String) {
    DRAFT("DRAFT"),
    PENDING_APPROVAL("PENDING_APPROVAL"),
    PUBLISHED("PUBLISHED"),
    ARCHIVED("ARCHIVED"),
    REJECTED("REJECTED")
}

enum class StudySetVisibility(val value: String) {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE")
}

enum class QuizType(val value: String) {
    MULTIPLE_CHOICE("MULTIPLE_CHOICE"),
    TRUE_FALSE("TRUE_FALSE"),
    SHORT_ANSWER("SHORT_ANSWER")
}

