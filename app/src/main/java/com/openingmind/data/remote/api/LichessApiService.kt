package com.openingmind.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface LichessApiService {
    @GET("api/volume")
    suspend fun getPopularOpenings(): LichessOpeningsDto

    @GET("https://explorer.lichess.org/lichess")
    suspend fun getOpeningExplorer(
        @Header("Authorization") token: String,
        @Query("play") moves: String? = null,
        @Query("fen") fen: String? = null,
        @Query("speeds") speeds: String = "blitz,rapid,classical,correspondence",
        @Query("ratings") ratings: String = "1600,1800,2000,2200,2500"
    ): LichessExplorerResponse
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

data class LichessExplorerResponse(
    val white: Int,
    val draws: Int,
    val black: Int,
    val moves: List<LichessExplorerMove>,
    val opening: LichessOpeningMeta?,
    val recentGames: List<LichessExplorerGame>?,
    val topGames: List<LichessExplorerGame>?
)

data class LichessOpeningMeta(
    val eco: String,
    val name: String
)

data class LichessExplorerMove(
    val uci: String,
    val san: String,
    val averageRating: Int,
    val white: Int,
    val draws: Int,
    val black: Int,
    val opening: LichessOpeningMeta?
)

data class LichessExplorerGame(
    val id: String,
    val winner: String?,
    val speed: String,
    val year: Int,
    val month: String?,
    val uci: String?,
    val white: LichessGamePlayer,
    val black: LichessGamePlayer
)

data class LichessGamePlayer(
    val name: String,
    val rating: Int
)
