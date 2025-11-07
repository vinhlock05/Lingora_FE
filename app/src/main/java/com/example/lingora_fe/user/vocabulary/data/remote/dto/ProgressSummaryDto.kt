package com.example.lingora_fe.user.vocabulary.data.remote.dto

import com.example.lingora_fe.core.network.ApiResponse
import com.google.gson.annotations.SerializedName

data class ProgressSummaryMetaData(
    @SerializedName("totalLearnedWord")
    val totalLearnedWord: Int?,
    @SerializedName("statistics")
    val statistics: List<StatisticItemDto>?
)

data class StatisticItemDto(
    @SerializedName("srsLevel")
    val srsLevel: Int,
    @SerializedName("wordCount")
    val wordCount: Int
)

typealias ProgressSummaryResponse = ApiResponse<ProgressSummaryMetaData>

