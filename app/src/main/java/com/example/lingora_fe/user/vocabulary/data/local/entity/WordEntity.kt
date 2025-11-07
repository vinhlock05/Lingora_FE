package com.example.lingora_fe.user.vocabulary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lingora_fe.admin.word.domain.model.CefrLevel
import com.example.lingora_fe.user.vocabulary.domain.model.Word

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey
    val id: Int,
    val topicId: Int,
    val phonetic: String?,
    val cefrLevel: CefrLevel?,
    val word: String,
    val meaning: String?,
    val example: String?,
    val exampleTranslation: String?,
    val audioUrl: String?,
    val imageUrl: String?
) {
    fun toDomain(): Word {
        return Word(
            id = id,
            topicId = topicId,
            phonetic = phonetic,
            cefrLevel = cefrLevel,
            word = word,
            meaning = meaning,
            example = example,
            exampleTranslation = exampleTranslation,
            audioUrl = audioUrl,
            imageUrl = imageUrl
        )
    }

    companion object {
        fun fromDomain(word: Word): WordEntity {
            return WordEntity(
                id = word.id,
                topicId = word.topicId,
                phonetic = word.phonetic,
                cefrLevel = word.cefrLevel,
                word = word.word,
                meaning = word.meaning,
                example = word.example,
                exampleTranslation = word.exampleTranslation,
                audioUrl = word.audioUrl,
                imageUrl = word.imageUrl
            )
        }
    }
}

