package com.openingmind.data.remote.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AzureAiService {
    @POST("openai/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") auth: String,
        @Body request: AzureChatRequest
    ): AzureChatResponse
}

data class AzureChatRequest(
    val model: String = "DeepSeek-V4-Flash",
    val messages: List<AzureMessage>,
    val max_tokens: Int = 16384,
    val temperature: Double = 0.8,
    val top_p: Double = 0.1,
    val presence_penalty: Double = 0.0,
    val frequency_penalty: Double = 0.0,
    val reasoning_effort: String = "none"
)

data class AzureMessage(
    val role: String,
    val content: String
)

data class AzureChatResponse(
    val id: String?,
    val created: Long?,
    val choices: List<AzureChoice>
)

data class AzureChoice(
    val message: AzureMessage
)
