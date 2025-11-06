package com.example.lingora_fe.admin.topic.domain.model

import com.example.lingora_fe.admin.word.domain.model.Word

data class TopicWithWords(
    val id: Int,
    val name: String,
    val description: String,
    val category: TopicCategory?,
    val totalWords: Int,
    val createdAt: String?,
    val words: List<Word>,
    val currentPage: Int,
    val totalPages: Int
)

