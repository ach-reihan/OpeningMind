package com.openingmind.data.repository

import com.openingmind.BuildConfig
import com.openingmind.data.local.dao.RepertoireDao
import com.openingmind.data.local.entity.RepertoireEntity
import com.openingmind.data.remote.api.AzureAiService
import com.openingmind.data.remote.api.AzureChatRequest
import com.openingmind.data.remote.api.AzureMessage
import com.openingmind.data.remote.api.LichessApiService
import com.openingmind.domain.model.Repertoire
import com.openingmind.domain.repository.RepertoireRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RepertoireRepositoryImpl @Inject constructor(
    private val dao: RepertoireDao,
    private val lichessApi: LichessApiService,
    private val azureApi: AzureAiService
) : RepertoireRepository {

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

    override suspend fun getRemoteOpenings(): List<Repertoire> {
        return try {
            val response = lichessApi.getPopularOpenings()
            response.openings.map { dto ->
                Repertoire(
                    ecoCode = dto.eco,
                    name = dto.name,
                    notation = dto.moves,
                    description = dto.desc ?: "Tidak ada deskripsi."
                )
            }
        } catch (e: Exception) {
            getFallbackOpenings()
        }
    }

    override suspend fun getAIChessAdvice(prompt: String): String {
        return try {
            val request = AzureChatRequest(
                messages = listOf(
                    AzureMessage(role = "system", content = "Kamu adalah pelatih dan grandmaster catur profesional. Berikan saran taktis pembukaan catur secara ringkas dalam 2-3 kalimat saja."),
                    AzureMessage(role = "user", content = prompt)
                )
            )
            val response = azureApi.getChatCompletion(
                apiKey = BuildConfig.AZURE_AI_KEY,
                request = request
            )
            response.choices.firstOrNull()?.message?.content ?: "Tidak ada respons dari asisten AI catur."
        } catch (e: Exception) {
            "Gagal mendapatkan saran taktis dari Azure AI Advisor: ${e.localizedMessage}"
        }
    }

    private fun getFallbackOpenings(): List<Repertoire> {
        return listOf(
            Repertoire(1, "B20", "Sicilian Defense", "1. e4 c5", "Tanggapan paling populer dan dinamis terhadap langkah e4."),
            Repertoire(2, "C50", "Giuoco Piano", "1. e4 e5 2. Nf3 Nc6 3. Bc4", "Pembukaan Italian Game klasik yang sangat solid.")
        )
    }
}