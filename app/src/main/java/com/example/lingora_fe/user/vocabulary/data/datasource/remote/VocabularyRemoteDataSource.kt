package com.example.lingora_fe.user.vocabulary.data.datasource.remote

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.vocabulary.data.remote.api.VocabularyApiService
import com.example.lingora_fe.user.vocabulary.data.remote.dto.*
import javax.inject.Inject

class VocabularyRemoteDataSource @Inject constructor(
    private val apiService: VocabularyApiService
) {
    // Word Progress operations
    suspend fun createWordProgress(request: CreateWordProgressRequest): CreateWordProgressResponse {
        return apiService.createWordProgress(request)
    }

    suspend fun updateWordProgress(request: UpdateWordProgressRequest): ApiResponse<WordProgressMetaData> {
        return apiService.updateWordProgress(request)
    }

    suspend fun getProgressSummary(): ProgressSummaryResponse {
        return apiService.getProgressSummary()
    }

    // Categories with progress
    suspend fun getCategoriesWithProgress(
        limit: Int = 20,
        page: Int = 1,
        search: String? = null
    ): CategoryProgressListResponse {
        return apiService.getCategoriesWithProgress(limit, page, search)
    }

    // Topics in category with progress
    suspend fun getCategoryTopicsWithProgress(
        categoryId: Int,
        limit: Int = 20,
        page: Int = 1,
        search: String? = null,
        sort: String? = null
    ): CategoryTopicProgressResponse {
        return apiService.getCategoryTopicsWithProgress(categoryId, limit, page, search, sort)
    }

    // Words for study
    suspend fun getWordsForStudy(topicId: Int, count: Int): StudyWordsResponse {
        val response = apiService.getWordsForStudy(topicId, count)
        android.util.Log.d("VocabularyRemoteDataSource", "Raw response: $response")
        android.util.Log.d("VocabularyRemoteDataSource", "Response metaData: ${response.metaData}")
        response.metaData?.words?.forEachIndexed { index, word ->
            android.util.Log.d("VocabularyRemoteDataSource", "Word[$index] DTO: id=${word.id}, word=${word.word}, phonetic=${word.phonetic}, audioUrl=${word.audioUrl}, meaning=${word.meaning}")
        }
        return response
    }

    // Words in topic with progress
    suspend fun getTopicWordsWithProgress(
        topicId: Int,
        limit: Int = 20,
        page: Int = 1,
        search: String? = null,
        hasLearned: Boolean? = null
    ): TopicWordProgressResponse {
        return apiService.getTopicWordsWithProgress(topicId, limit, page, search, hasLearned)
    }

    // Words for review
    suspend fun getWordsForReview(
        limit: Int = 20,
        page: Int = 1
    ): ReviewWordsResponse {
        return apiService.getWordsForReview(limit, page)
    }
}

