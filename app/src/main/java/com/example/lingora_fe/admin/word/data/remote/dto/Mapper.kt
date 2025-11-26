package com.example.lingora_fe.admin.word.data.remote.dto

import com.example.lingora_fe.admin.word.domain.model.Word

// DTO to Domain
fun WordDto.toDomain(): Word {
    return Word(
        id = id,
        word = word,
        phonetic = phonetic,
        cefrLevel = cefrLevel,
        type = type,
        meaning = meaning,
        vnMeaning = vnMeaning,
        example = example,
        exampleTranslation = exampleTranslation,
        audioUrl = audioUrl,
        imageUrl = imageUrl,
        topicId = topic?.id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}

