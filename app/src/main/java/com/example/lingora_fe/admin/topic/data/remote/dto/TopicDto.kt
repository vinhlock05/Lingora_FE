package com.example.lingora_fe.admin.topic.data.remote.dto

import com.example.lingora_fe.admin.word.data.remote.dto.WordDto
import com.example.lingora_fe.core.network.SerializeNull
import com.google.gson.annotations.SerializedName

// MetaData DTOs
data class TopicListMetaData(
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("topics")
    val topics: List<TopicDto>
)

data class CategoryWithTopicsDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("totalTopics")
    val totalTopics: Int,
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("topics")
    val topics: List<TopicDto>
)

data class TopicWithWordsDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("category")
    val category: TopicCategoryDto?,
    @SerializedName("totalWords")
    val totalWords: Int,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("words")
    val words: List<WordDto>
)

data class TopicDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("category")
    val category: TopicCategoryDto?,
    @SerializedName("totalWords")
    val totalWords: Int = 0,
    @SerializedName("createdAt")
    val createdAt: String?
)

data class TopicCategoryDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("createdAt")
    val createdAt: String?
)

// Request DTOs
data class CreateTopicRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("categoryId")
    val categoryId: Int?
)

data class UpdateTopicRequest(
    @SerializedName("name")
    val name: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("categoryId")
    @SerializeNull val categoryId: Int?
)

