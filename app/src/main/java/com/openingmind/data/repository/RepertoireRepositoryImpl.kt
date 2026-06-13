package com.openingmind.data.repository

import android.content.Context
import com.openingmind.BuildConfig
import com.openingmind.R
import com.openingmind.data.local.dao.RepertoireDao
import com.openingmind.data.local.entity.RemoteOpeningEntity
import com.openingmind.data.local.entity.RepertoireEntity
import com.openingmind.data.remote.api.AzureAiService
import com.openingmind.data.remote.api.AzureChatRequest
import com.openingmind.data.remote.api.AzureMessage
import com.openingmind.data.remote.api.LichessApiService
import com.openingmind.domain.model.Repertoire
import com.openingmind.domain.repository.RepertoireRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RepertoireRepositoryImpl @Inject constructor(
    private val dao: RepertoireDao,
    private val remoteOpeningDao: com.openingmind.data.local.dao.RemoteOpeningDao,
    private val lichessApi: LichessApiService,
    private val azureApi: AzureAiService,
    @ApplicationContext private val context: Context
) : RepertoireRepository {

    private fun getLocalizedContext(language: String): Context {
        val locale = java.util.Locale(language)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    override fun getLocalRepertoires(): Flow<List<Repertoire>> {
        return dao.getAllRepertoires().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLocalRepertoireById(id: Int): Repertoire? {
        return dao.getRepertoireById(id)?.toDomain()
    }

    override suspend fun insertRepertoire(repertoire: Repertoire) {
        dao.insertRepertoire(RepertoireEntity.fromDomain(repertoire))
    }

    override suspend fun updateRepertoire(repertoire: Repertoire) {
        dao.updateRepertoire(RepertoireEntity.fromDomain(repertoire))
    }

    override suspend fun deleteRepertoire(repertoire: Repertoire) {
        dao.deleteRepertoire(RepertoireEntity.fromDomain(repertoire))
    }

    override suspend fun getRemoteOpenings(language: String): List<Repertoire> {
        val lichessToken = BuildConfig.LICHESS_TOKEN
        val localizedContext = getLocalizedContext(language)
        
        return try {
            val token = "Bearer $lichessToken"
            val result = mutableListOf<Repertoire>()

            // 1. Fetch initial popular openings from the explorer
            val explorerResponse = lichessApi.getOpeningExplorer(token = token)
            
            // Map moves that have an associated opening
            explorerResponse.moves.forEach { move ->
                move.opening?.let { meta ->
                    val total = move.white + move.draws + move.black
                    val stats = if (total > 0) {
                        localizedContext.getString(
                            R.string.lichess_stats_info,
                            (move.white * 100) / total,
                            (move.draws * 100) / total,
                            (move.black * 100) / total
                        )
                    } else ""

                    val avgRating = if (move.averageRating > 0) {
                        localizedContext.getString(R.string.lichess_avg_rating, move.averageRating)
                    } else ""

                    val desc = listOfNotNull(
                        localizedContext.getString(R.string.lichess_popular_opening, move.san),
                        stats.takeIf { it.isNotEmpty() },
                        avgRating.takeIf { it.isNotEmpty() }
                    ).joinToString(" ")

                    result.add(
                        Repertoire(
                            id = move.uci.hashCode(),
                            ecoCode = meta.eco,
                            name = meta.name,
                            notation = move.san,
                            description = desc
                        )
                    )

                    // Deep fetch: get variations for each primary move
                    try {
                        val subResponse = lichessApi.getOpeningExplorer(token = token, moves = move.uci)
                        subResponse.moves.take(3).forEach { subMove ->
                            subMove.opening?.let { subMeta ->
                                if (result.none { it.ecoCode == subMeta.eco }) {
                                    val subTotal = subMove.white + subMove.draws + subMove.black
                                    val subStats = if (subTotal > 0) {
                                        localizedContext.getString(
                                            R.string.lichess_stats_info,
                                            (subMove.white * 100) / subTotal,
                                            (subMove.draws * 100) / subTotal,
                                            (subMove.black * 100) / subTotal
                                        )
                                    } else ""

                                    val subAvg = if (subMove.averageRating > 0) {
                                        localizedContext.getString(R.string.lichess_avg_rating, subMove.averageRating)
                                    } else ""

                                    val subDesc = listOfNotNull(
                                        localizedContext.getString(R.string.lichess_popular_variation, meta.name),
                                        subStats.takeIf { it.isNotEmpty() },
                                        subAvg.takeIf { it.isNotEmpty() }
                                    ).joinToString(" ")

                                    result.add(
                                        Repertoire(
                                            id = (move.uci + subMove.uci).hashCode(),
                                            ecoCode = subMeta.eco,
                                            name = subMeta.name,
                                            notation = "${move.san} ${subMove.san}",
                                            description = subDesc
                                        )
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore sub-fetch failures
                    }
                }
            }

            // 2. Fetch the current position's opening if available
            explorerResponse.opening?.let { meta ->
                if (result.none { it.ecoCode == meta.eco }) {
                    val total = explorerResponse.white + explorerResponse.draws + explorerResponse.black
                    val stats = if (total > 0) {
                        localizedContext.getString(
                            R.string.lichess_stats_info,
                            (explorerResponse.white * 100) / total,
                            (explorerResponse.draws * 100) / total,
                            (explorerResponse.black * 100) / total
                        )
                    } else ""

                    val desc = listOfNotNull(
                        localizedContext.getString(R.string.lichess_initial_position, meta.name),
                        stats.takeIf { it.isNotEmpty() }
                    ).joinToString(" ")

                    result.add(
                        0,
                        Repertoire(
                            id = meta.hashCode(),
                            ecoCode = meta.eco,
                            name = meta.name,
                            notation = "",
                            description = desc
                        )
                    )
                }
            }

            // 3. Supplement with the volume API if results are few
            if (result.size < 10) {
                try {
                    val popularResponse = lichessApi.getPopularOpenings()
                    popularResponse.openings.forEach { dto ->
                        if (result.none { it.ecoCode == dto.eco }) {
                            result.add(
                                Repertoire(
                                    id = dto.hashCode(),
                                    ecoCode = dto.eco,
                                    name = dto.name,
                                    notation = dto.moves,
                                    description = dto.desc ?: localizedContext.getString(R.string.lichess_info_fallback)
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RepertoireRepo", "Volume API failed", e)
                }
            }
            
            val finalResult = result.sortedBy { it.name }
            
            // Save to cache (Primary Fallback)
            if (finalResult.isNotEmpty()) {
                remoteOpeningDao.clearRemoteOpeningsByLanguage(language)
                remoteOpeningDao.insertRemoteOpenings(finalResult.map { 
                    RemoteOpeningEntity.fromDomain(it, language) 
                })
            }
            
            finalResult
        } catch (e: Exception) {
            android.util.Log.e("RepertoireRepo", "Explorer API failed. Checking cache...", e)
            
            // Try loading from cache (Primary Fallback)
            val cachedOpenings = remoteOpeningDao.getRemoteOpeningsByLanguage(language)
            if (cachedOpenings.isNotEmpty()) {
                android.util.Log.d("RepertoireRepo", "Using cached remote openings.")
                cachedOpenings.map { it.toDomain() }
            } else {
                android.util.Log.d("RepertoireRepo", "No cache available. Using hardcoded fallback.")
                // Hardcoded secondary fallback
                getFallbackOpenings(localizedContext)
            }
        }
    }

    override suspend fun getRemoteOpeningByMoves(moves: String, language: String): Repertoire? {
        val localizedContext = getLocalizedContext(language)
        return try {
            val token = "Bearer ${BuildConfig.LICHESS_TOKEN}"
            // Use the specific move string (UCI or SAN) to get opening details
            val response = lichessApi.getOpeningExplorer(token = token, moves = moves)
            
            val total = response.white + response.draws + response.black
            val stats = if (total > 0) {
                localizedContext.getString(
                    R.string.lichess_stats_info,
                    (response.white * 100) / total,
                    (response.draws * 100) / total,
                    (response.black * 100) / total
                )
            } else ""

            response.opening?.let { meta ->
                val desc = listOfNotNull(
                    localizedContext.getString(R.string.lichess_analysis_desc),
                    stats.takeIf { it.isNotEmpty() }
                ).joinToString(" ")

                Repertoire(
                    id = moves.hashCode(),
                    ecoCode = meta.eco,
                    name = meta.name,
                    notation = moves,
                    description = desc
                )
            } ?: run {
                // If no root opening, check if the last move has metadata
                val lastMoveWithOpening = response.moves.lastOrNull { it.opening != null }
                lastMoveWithOpening?.opening?.let { meta ->
                    val desc = listOfNotNull(
                        localizedContext.getString(R.string.lichess_variation_desc, meta.name),
                        stats.takeIf { it.isNotEmpty() }
                    ).joinToString(" ")

                    Repertoire(
                        id = moves.hashCode(),
                        ecoCode = meta.eco,
                        name = meta.name,
                        notation = moves,
                        description = desc
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("RepertoireRepo", "Error fetching opening by moves: $moves", e)
            null
        }
    }

    override suspend fun getAIChessAdvice(prompt: String, systemPrompt: String): String {
        return try {
            val request = AzureChatRequest(
                messages = listOf(
                    AzureMessage(role = "system", content = systemPrompt),
                    AzureMessage(role = "user", content = prompt)
                )
            )
            android.util.Log.d("RepertoireRepo", "Requesting AI advice with prompt: $prompt")
            // Use the API key as a Bearer token in the Authorization header
            val response = azureApi.getChatCompletion(
                auth = "Bearer ${BuildConfig.AZURE_AI_KEY}",
                request = request
            )
            val result = response.choices.firstOrNull()?.message?.content ?: "AI response fallback"
            android.util.Log.d("RepertoireRepo", "AI Response received successfully")
            result
        } catch (e: Exception) {
            android.util.Log.e("RepertoireRepo", "Azure AI Error: ${e.localizedMessage}", e)
            "AI error fallback"
        }
    }

    private fun getFallbackOpenings(localizedContext: Context): List<Repertoire> {
        return listOf(
            Repertoire(1, "B20", localizedContext.getString(R.string.fallback_sicilian_name), "1. e4 c5", localizedContext.getString(R.string.fallback_sicilian_desc)),
            Repertoire(2, "C50", localizedContext.getString(R.string.fallback_giuoco_name), "1. e4 e5 2. Nf3 Nc6 3. Bc4 Bc5", localizedContext.getString(R.string.fallback_giuoco_desc))
        )
    }
}