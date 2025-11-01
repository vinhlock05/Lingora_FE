package com.example.lingora_fe.user.vocabulary.domain.model

import java.sql.Timestamp

data class Topic(
    val id: Int,
    val name: String,
    val description: String,
    val createdAt: Timestamp
)

