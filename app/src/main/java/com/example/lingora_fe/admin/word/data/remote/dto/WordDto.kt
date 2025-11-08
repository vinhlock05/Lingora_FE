package com.example.lingora_fe.admin.word.data.remote.dto

import com.example.lingora_fe.core.network.SerializeNull
import com.google.gson.annotations.SerializedName

data class WordDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("word")
    val word: String,
    @SerializedName("phonetic")
    val phonetic: String?,
    @SerializedName("cefrLevel")
    val cefrLevel: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("meaning")
    val meaning: String,
    @SerializedName("example")
    val example: String?,
    @SerializedName("exampleTranslation")
    val exampleTranslation: String?,
    @SerializedName("audioUrl")
    val audioUrl: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("topic")
    val topic: TopicRefDto?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("deletedAt")
    val deletedAt: String?
)

data class TopicRefDto(
    @SerializedName("id")
    val id: Int?
)

data class WordListMetaData(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val totalWords: Int? = null, // For topic/{id}/words response
    val words: List<WordDto>
)

data class CreateWordRequest(
    val word: String,
    val meaning: String,
    val phonetic: String?,
    val cefrLevel: String,
    val type: String,
    val example: String?,
    val exampleTranslation: String?,
    val audioUrl: String?,
    val imageUrl: String?,
    val topicId: Int?
)

data class UpdateWordRequest(
    val word: String? = null,
    val meaning: String? = null,
    val phonetic: String? = null,
    val cefrLevel: String? = null,
    val type: String? = null,
    val example: String? = null,
    val exampleTranslation: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    @SerializeNull val topicId: Int? = null
)
