package com.example.lingora_fe.user.vocabulary.data.remote.dto

import com.example.lingora_fe.admin.word.domain.model.WordType
import com.example.lingora_fe.core.network.ApiResponse
import com.google.gson.annotations.SerializedName

data class TopicProgressDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("totalWords")
    val totalWords: Int,
    @SerializedName("learnedWords")
    val learnedWords: Int,
    @SerializedName("completed")
    val completed: Boolean
)

data class CategoryTopicProgressMetaData(
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("totalTopics")
    val totalTopics: Int,
    @SerializedName("completedTopics")
    val completedTopics: Int,
    @SerializedName("progressPercent")
    val progressPercent: Float,
    @SerializedName("completed")
    val completed: Boolean,
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("topics")
    val topics: List<TopicProgressDto>
)

typealias CategoryTopicProgressResponse = com.example.lingora_fe.core.network.ApiResponse<CategoryTopicProgressMetaData>

data class TopicWordProgressMetaData(
    @SerializedName("topicId")
    val topicId: Int,
    @SerializedName("totalWordsAll")
    val totalWordsAll: Int,
    @SerializedName("learnedCountAll")
    val learnedCountAll: Int,
    @SerializedName("completed")
    val completed: Boolean,
    @SerializedName("progressPercent")
    val progressPercent: Float,
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("totalWordsFiltered")
    val totalWordsFiltered: Int,
    @SerializedName("words")
    val words: List<WordWithProgressDto>
)

typealias TopicWordProgressResponse = ApiResponse<TopicWordProgressMetaData>
data class StudyWordsMetaData(
    @SerializedName("topicId")
    val topicId: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("words")
    val words: List<WordDto>
)
typealias StudyWordsResponse = ApiResponse<StudyWordsMetaData>

data class ReviewWordsMetaData(
    @SerializedName("page")
    val page: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("words")
    val words: List<WordDto> // Review API returns WordDto, not WordWithProgressDto
)

typealias ReviewWordsResponse = ApiResponse<ReviewWordsMetaData>

