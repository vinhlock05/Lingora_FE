package com.example.lingora_fe.admin.word.data.remote.dto

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

