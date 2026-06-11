package com.openingmind.data.remote.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AzureAiService {
    @POST("chat/completions?api-version=2024-05-01-preview")
    suspend fun getChatCompletion(
        @Header("Authorization") apiKey: String,
        @Body request: AzureChatRequest
    ): AzureChatResponse
}

data class AzureChatRequest(
    val messages: List<AzureMessage>,
    val model: String = "DeepSeek-V4-Flash",
    val max_tokens: Int = 300
)

data class AzureMessage(
    val role: String,
    val content: String
)

data class AzureChatResponse(
    val choices: List<AzureChoice>
)

data class AzureChoice(
    val message: AzureMessage
)