package com.openingmind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.openingmind.data.local.entity.RemoteOpeningEntity

@Dao
interface RemoteOpeningDao {
    @Query("SELECT * FROM remote_openings WHERE language = :language")
    suspend fun getRemoteOpeningsByLanguage(language: String): List<RemoteOpeningEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteOpenings(entities: List<RemoteOpeningEntity>)

    @Query("DELETE FROM remote_openings WHERE language = :language")
    suspend fun clearRemoteOpeningsByLanguage(language: String)
}
