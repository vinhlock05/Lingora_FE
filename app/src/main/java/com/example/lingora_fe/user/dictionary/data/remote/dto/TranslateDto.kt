package com.example.lingora_fe.user.dictionary.data.remote.dto

import com.example.lingora_fe.user.dictionary.domain.model.TranslateResult
import com.google.gson.annotations.SerializedName

data class TranslatePhraseRequest(
    @SerializedName("text")
    val text: String,
    @SerializedName("sourceLang")
    val sourceLang: String? = null,
    @SerializedName("targetLang")
    val targetLang: String? = null
)

data class TranslatePhraseDto(
    @SerializedName("originalText")
    val originalText: String,
    @SerializedName("translatedText")
    val translatedText: String,
    @SerializedName("from")
    val from: String,
    @SerializedName("to")
    val to: String
) {
    fun toDomain(): TranslateResult = TranslateResult(
        originalText = originalText,
        translatedText = translatedText,
        from = from,
        to = to
    )
}



