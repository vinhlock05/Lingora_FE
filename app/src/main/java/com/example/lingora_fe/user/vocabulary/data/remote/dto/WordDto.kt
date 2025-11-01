package com.example.lingora_fe.user.vocabulary.data.remote.dto

import com.example.lingora_fe.user.vocabulary.domain.model.Word
import com.google.gson.annotations.SerializedName

data class WordDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("topic_id")
    val topicId: Int,
    @SerializedName("level")
    val level: String,
    @SerializedName("word")
    val word: String,
    @SerializedName("meaning")
    val meaning: String,
    @SerializedName("example")
    val example: String,
    @SerializedName("example_translation")
    val exampleTranslation: String,
    @SerializedName("position")
    val position: Int,
    @SerializedName("audio_url")
    val audioUrl: String,
    @SerializedName("image_url")
    val imageUrl: String
) {
    fun toDomain(): Word {
        return Word(
            id = id,
            topicId = topicId,
            level = level,
            word = word,
            meaning = meaning,
            example = example,
            exampleTranslation = exampleTranslation,
            position = position,
            audioUrl = audioUrl,
            imageUrl = imageUrl
        )
    }
}

data class WordResponse(
    @SerializedName("data")
    val data: WordDto
)

data class WordsResponse(
    @SerializedName("data")
    val data: List<WordDto>
)

