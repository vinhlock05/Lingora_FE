package com.example.lingora_fe.user.vocabulary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lingora_fe.user.vocabulary.domain.model.Word

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey
    val id: Int,
    val topicId: Int,
    val level: String,
    val word: String,
    val meaning: String,
    val example: String,
    val exampleTranslation: String,
    val position: Int,
    val audioUrl: String,
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

    companion object {
        fun fromDomain(word: Word): WordEntity {
            return WordEntity(
                id = word.id,
                topicId = word.topicId,
                level = word.level,
                word = word.word,
                meaning = word.meaning,
                example = word.example,
                exampleTranslation = word.exampleTranslation,
                position = word.position,
                audioUrl = word.audioUrl,
                imageUrl = word.imageUrl
            )
        }
    }
}

