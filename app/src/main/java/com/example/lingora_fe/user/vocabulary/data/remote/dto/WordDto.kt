package com.example.lingora_fe.user.vocabulary.data.remote.dto

import com.example.lingora_fe.admin.word.domain.model.CefrLevel
import com.example.lingora_fe.user.vocabulary.domain.model.Word
import com.google.gson.annotations.SerializedName

data class WordDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("topicId")
    val topicId: Int,
    @SerializedName("phonetic")
    val phonetic: String?,
    @SerializedName("cefrLevel")
    val cefrLevel: CefrLevel?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("word")
    val word: String,
    @SerializedName("meaning")
    val meaning: String?,
    @SerializedName("vnMeaning")
    val vnMeaning: String?,
    @SerializedName("example")
    val example: String?,
    @SerializedName("exampleTranslation")
    val exampleTranslation: String?,
    @SerializedName("audioUrl")
    val audioUrl: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?
) {
    fun toDomain(): Word {
        return Word(
            id = id,
            topicId = topicId,
            phonetic = phonetic,
            cefrLevel = cefrLevel ?: CefrLevel.A1, // Default to A1 if null
            type = type,
            word = word,
            meaning = meaning,
            vnMeaning = vnMeaning,
            example = example,
            exampleTranslation = exampleTranslation,
            audioUrl = audioUrl,
            imageUrl = imageUrl
        )
    }
}

