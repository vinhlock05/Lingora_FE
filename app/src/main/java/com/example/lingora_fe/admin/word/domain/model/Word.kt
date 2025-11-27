package com.example.lingora_fe.admin.word.domain.model

data class Word(
    val id: Int,
    val word: String,
    val phonetic: String?,
    val cefrLevel: String,
    val type: String,
    val meaning: String,
    val vnMeaning: String?,
    val example: String?,
    val exampleTranslation: String?,
    val audioUrl: String?,
    val imageUrl: String?,
    val topicId: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val deletedAt: String?
)

// Enums for type safety
enum class CefrLevel(val value: String) {
    A1("A1"),
    A2("A2"),
    B1("B1"),
    B2("B2"),
    C1("C1"),
    C2("C2")
}

enum class WordType(val value: String) {
    NOUN("noun"),
    VERB("verb"),
    ADJECTIVE("adjective"),
    ADVERB("adverb"),
    PHRASE("phrase"),
    PREPOSITION("preposition"),
    CONJUNCTION("conjunction"),
    INTERJECTION("interjection"),
    PRONOUN("pronoun"),
    DETERMINER("determiner"),
    ARTICLE("article"),
    NUMERAL("numeral"),
    UNKNOWN("unknown")
}

