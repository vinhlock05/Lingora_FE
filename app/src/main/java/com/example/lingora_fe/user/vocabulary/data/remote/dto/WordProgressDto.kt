package com.example.lingora_fe.user.vocabulary.data.remote.dto

import com.example.lingora_fe.user.vocabulary.domain.model.Word
import com.example.lingora_fe.user.vocabulary.domain.model.WordProgress
import com.example.lingora_fe.user.vocabulary.domain.model.WordStatus
import com.example.lingora_fe.user.vocabulary.domain.model.WordWithProgress
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class WordProgressDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("srsLevel")
    val srsLevel: Int,
    @SerializedName("learnedAt")
    val learnedAt: String?,
    @SerializedName("nextReviewDay")
    val nextReviewDay: String?,
    @SerializedName("wrongCount")
    val wrongCount: Int? = 0,
    @SerializedName("reviewedDate")
    val reviewedDate: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun toDomain(wordId: Int, userId: Int): WordProgress {
        return WordProgress(
            id = id,
            wordId = wordId,
            userId = userId,
            status = WordStatus.values().find { it.value == status } ?: WordStatus.NEW,
            srsLevel = srsLevel,
            learnedAt = learnedAt?.let { parseDate(it) },
            nextReviewDay = nextReviewDay?.let { parseDate(it) },
            wrongCount = wrongCount ?: 0,
            reviewedDate = reviewedDate?.let { parseDate(it) },
            createdAt = createdAt?.let { parseDate(it) },
            updatedAt = updatedAt?.let { parseDate(it) }
        )
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}

data class WordWithProgressDto(
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
    val imageUrl: String?,
    @SerializedName("progress")
    val progress: WordProgressDto?
) {
    fun toDomain(): WordWithProgress {
        return WordWithProgress(
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
            progress = progress?.toDomain(id, 0) // userId will be set in
        )
    }
}

data class CreateWordProgressRequest(
    @SerializedName("wordIds")
    val wordIds: List<Int>
)

data class WordProgressRequest(
    @SerializedName("wordId")
    val wordId: Int,
    @SerializedName("wrongCount")
    val wrongCount: Int,
    @SerializedName("reviewedDate")
    val reviewedDate: String
)

data class UpdateWordProgressRequest(
    @SerializedName("wordProgress")
    val wordProgress: List<WordProgressRequest>
)

data class CreateWordProgressResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("metaData")
    val metaData: WordProgressMetaData?
)

data class WordProgressMetaData(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("totalCreated")
    val totalCreated: Int? = null,
    @SerializedName("totalUpdated")
    val totalUpdated: Int? = null,
    @SerializedName("wordProgresses")
    val wordProgresses: List<WordProgressWithWordDto>?
)

data class WordProgressWithWordDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("word")
    val word: WordDto?, // Can be null if API doesn't return word data
    @SerializedName("status")
    val status: String,
    @SerializedName("srsLevel")
    val srsLevel: Int,
    @SerializedName("learnedAt")
    val learnedAt: String?,
    @SerializedName("nextReviewDay")
    val nextReviewDay: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
) {
    fun toDomain(userId: Int): WordProgress? {
        // Return null if word is null (can't create WordProgress without wordId)
        val wordId = word?.id ?: return null
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        
        fun parseDate(dateString: String?): Date? {
            return dateString?.let {
                try {
                    dateFormat.parse(it)
                } catch (e: Exception) {
                    null
                }
            }
        }
        
        return WordProgress(
            id = id,
            wordId = wordId,
            userId = userId,
            status = WordStatus.values().find { it.value == status } ?: WordStatus.NEW,
            srsLevel = srsLevel,
            learnedAt = parseDate(learnedAt),
            nextReviewDay = parseDate(nextReviewDay),
            wrongCount = 0,
            reviewedDate = null,
            createdAt = parseDate(createdAt),
            updatedAt = parseDate(updatedAt)
        )
    }
}

