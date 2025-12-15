package com.example.lingora_fe.user.studyset.data.remote.dto

import com.example.lingora_fe.user.studyset.domain.model.BuyStudySetResponse
import com.example.lingora_fe.user.studyset.domain.model.CreateStudySetData
import com.example.lingora_fe.user.studyset.domain.model.Flashcard
import com.example.lingora_fe.user.studyset.domain.model.Owner
import com.example.lingora_fe.user.studyset.domain.model.Quiz
import com.example.lingora_fe.user.studyset.domain.model.QuizType
import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.studyset.domain.model.StudySetListMetadata
import com.example.lingora_fe.user.studyset.domain.model.StudySetStatus
import com.example.lingora_fe.user.studyset.domain.model.StudySetVisibility
import com.example.lingora_fe.user.studyset.domain.model.UpdateStudySetData

// DTO to Domain
fun StudySetDto.toDomain(): StudySet {
    return StudySet(
        id = id,
        title = title,
        description = description,
        visibility = StudySetVisibility.values().find { it.value == visibility } ?: StudySetVisibility.PRIVATE,
        price = price,
        status = StudySetStatus.values().find { it.value == status } ?: StudySetStatus.DRAFT,
        likeCount = likeCount,
        commentCount = commentCount ?: 0,
        createdAt = createdAt,
        updatedAt = updatedAt,
        owner = owner.toDomain(),
        flashcards = flashcards?.map { it.toDomain() } ?: emptyList(),
        quizzes = quizzes?.map { it.toDomain() } ?: emptyList(),
        totalFlashcards = totalFlashcards,
        totalQuizzes = totalQuizzes,
        isPurchased = isPurchased,
        isAlreadyLike = isAlreadyLike ?: false,
        comments = comments?.map { it.toDomain() } ?: emptyList()
    )
}

fun OwnerDto.toDomain(): Owner {
    return Owner(
        id = id,
        username = username
    )
}

fun FlashcardDto.toDomain(): Flashcard {
    return Flashcard(
        id = id,
        frontText = frontText,
        backText = backText,
        example = example,
        audioUrl = audioUrl,
        imageUrl = imageUrl
    )
}

fun QuizDto.toDomain(): Quiz {
    return Quiz(
        id = id,
        type = QuizType.values().find { it.value == type } ?: QuizType.MULTIPLE_CHOICE,
        question = question,
        options = options,
        correctAnswer = correctAnswer
    )
}

fun StudySetListMetaData.toDomain(): StudySetListMetadata {
    return StudySetListMetadata(
        currentPage = currentPage,
        totalPages = totalPages,
        total = total,
        studySets = studySets.map { it.toDomain() }
    )
}

fun BuyStudySetResponseDto.toDomain(): BuyStudySetResponse {
    return BuyStudySetResponse(
        paymentUrl = paymentUrl,
        isFree = isFree,
        message = message,
        orderId = orderId,
        amount = amount,
        transactionId = transactionId
    )
}

// Domain to DTO
fun CreateStudySetData.toDto(): CreateStudySetRequest {
    return CreateStudySetRequest(
        title = title,
        description = description,
        visibility = visibility.value,
        price = price.takeIf { it > 0 },
        flashcards = flashcards.map { it.toDto() },
        quizzes = quizzes.map { it.toDto() }
    )
}

fun UpdateStudySetData.toDto(): UpdateStudySetRequest {
    return UpdateStudySetRequest(
        title = title,
        description = description,
        visibility = visibility?.value,
        price = price,
        status = status?.value,
        flashcards = flashcards?.map { it.toDto() },
        quizzes = quizzes?.map { it.toDto() }
    )
}

fun Flashcard.toDto(): FlashcardDto {
    return FlashcardDto(
        id = id,
        frontText = frontText,
        backText = backText,
        example = example,
        audioUrl = audioUrl,
        imageUrl = imageUrl
    )
}

fun Quiz.toDto(): QuizDto {
    return QuizDto(
        id = id,
        type = type.value,
        question = question,
        options = options,
        correctAnswer = correctAnswer
    )
}

