package com.openingmind.data.remote.api

import retrofit2.http.GET

interface LichessApiService {
    @GET("api/volume")
    suspend fun getPopularOpenings(): LichessOpeningsDto
}

data class LichessOpeningsDto(
    val openings: List<LichessOpeningDto>
)

data class LichessOpeningDto(
    val eco: String,
    val name: String,
    val moves: String,
    val desc: String?
)