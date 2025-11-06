package com.example.lingora_fe.admin.topic.data.remote.dto

import com.example.lingora_fe.admin.topic.domain.model.*
import com.example.lingora_fe.admin.word.data.remote.dto.toDomain

// DTO to Domain
fun TopicListMetaData.toDomain(): TopicListMetadata {
    return TopicListMetadata(
        currentPage = currentPage,
        totalPages = totalPages,
        total = total,
        topics = topics.map { it.toDomain() }
    )
}

fun CategoryWithTopicsDto.toDomain(): CategoryWithTopics {
    return CategoryWithTopics(
        id = id,
        name = name,
        description = description,
        totalTopics = totalTopics,
        currentPage = currentPage,
        totalPages = totalPages,
        topics = topics.map { it.toDomain() }
    )
}

fun TopicWithWordsDto.toDomain(): TopicWithWords {
    return TopicWithWords(
        id = id,
        name = name,
        description = description,
        category = category?.toDomain(),
        totalWords = totalWords,
        createdAt = createdAt,
        currentPage = currentPage,
        totalPages = totalPages,
        words = words.map { it.toDomain() }
    )
}

fun TopicDto.toDomain(): Topic {
    return Topic(
        id = id,
        name = name,
        description = description,
        category = category?.toDomain(),
        totalWords = totalWords,
        createdAt = createdAt
    )
}

fun TopicCategoryDto.toDomain(): TopicCategory {
    return TopicCategory(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt
    )
}

// Domain to DTO
fun CreateTopicData.toDto(): CreateTopicRequest {
    return CreateTopicRequest(
        name = name,
        description = description,
        categoryId = categoryId
    )
}

fun UpdateTopicData.toDto(): UpdateTopicRequest {
    return UpdateTopicRequest(
        name = name,
        description = description,
        categoryId = categoryId
    )
}

