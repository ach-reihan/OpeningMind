package com.openingmind.data.local.dao

import androidx.room.*
import com.openingmind.data.local.entity.RepertoireEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepertoireDao {
    @Query("SELECT * FROM repertoires")
    fun getAllRepertoires(): Flow<List<RepertoireEntity>>

    @Query("SELECT * FROM repertoires WHERE id = :id")
    suspend fun getRepertoireById(id: Int): RepertoireEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepertoire(entity: RepertoireEntity)

    @Update
    suspend fun updateRepertoire(entity: RepertoireEntity)

    @Delete
    suspend fun deleteRepertoire(entity: RepertoireEntity)
}