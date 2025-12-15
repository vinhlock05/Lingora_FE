package com.example.lingora_fe.user.studyset.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.lingora_fe.user.forum.data.remote.dto.CommentDto

// MetaData DTOs
data class StudySetListMetaData(
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("studySets")
    val studySets: List<StudySetDto>
)

data class StudySetDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("visibility")
    val visibility: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("likeCount")
    val likeCount: Int,
    @SerializedName("commentCount")
    val commentCount: Int? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("owner")
    val owner: OwnerDto,
    @SerializedName("flashcards")
    val flashcards: List<FlashcardDto>? = null,
    @SerializedName("quizzes")
    val quizzes: List<QuizDto>? = null,
    @SerializedName("totalFlashcards")
    val totalFlashcards: Int? = null,
    @SerializedName("totalQuizzes")
    val totalQuizzes: Int? = null,
    @SerializedName("isPurchased")
    val isPurchased: Boolean? = null,
    @SerializedName("isAlreadyLike")
    val isAlreadyLike: Boolean? = null,
    @SerializedName("comments")
    val comments: List<CommentDto>? = null
)

data class OwnerDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val username: String
)

data class FlashcardDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("frontText")
    val frontText: String,
    @SerializedName("backText")
    val backText: String,
    @SerializedName("example")
    val example: String? = null,
    @SerializedName("audioUrl")
    val audioUrl: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null
)

data class QuizDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("type")
    val type: String,
    @SerializedName("question")
    val question: String,
    @SerializedName("options")
    val options: List<String>,
    @SerializedName("correctAnswer")
    val correctAnswer: String
)

// Request DTOs
data class CreateStudySetRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("visibility")
    val visibility: String? = null,
    @SerializedName("price")
    val price: Int? = null,
    @SerializedName("flashcards")
    val flashcards: List<FlashcardDto> = emptyList(),
    @SerializedName("quizzes")
    val quizzes: List<QuizDto> = emptyList()
)

data class UpdateStudySetRequest(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("visibility")
    val visibility: String? = null,
    @SerializedName("price")
    val price: Int? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("flashcards")
    val flashcards: List<FlashcardDto>? = null,
    @SerializedName("quizzes")
    val quizzes: List<QuizDto>? = null
)

data class BuyStudySetResponseDto(
    @SerializedName("paymentUrl")
    val paymentUrl: String? = null,
    @SerializedName("isFree")
    val isFree: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("orderId")
    val orderId: String? = null,
    @SerializedName("amount")
    val amount: Int? = null,
    @SerializedName("transactionId")
    val transactionId: Int? = null
)

