package com.openingmind.domain.repository

import com.openingmind.domain.model.Repertoire
import kotlinx.coroutines.flow.Flow

interface RepertoireRepository {
    fun getLocalRepertoires(): Flow<List<Repertoire>>
    suspend fun getLocalRepertoireById(id: Int): Repertoire?
    suspend fun insertRepertoire(repertoire: Repertoire)
    suspend fun updateRepertoire(repertoire: Repertoire)
    suspend fun deleteRepertoire(repertoire: Repertoire)
    suspend fun getRemoteOpenings(): List<Repertoire>
    suspend fun getAIChessAdvice(prompt: String): String
}