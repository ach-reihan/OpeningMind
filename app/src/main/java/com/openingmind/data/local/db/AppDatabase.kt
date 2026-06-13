package com.openingmind.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.openingmind.data.local.dao.RemoteOpeningDao
import com.openingmind.data.local.dao.RepertoireDao
import com.openingmind.data.local.entity.RemoteOpeningEntity
import com.openingmind.data.local.entity.RepertoireEntity

@Database(entities = [RepertoireEntity::class, RemoteOpeningEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val repertoireDao: RepertoireDao
    abstract val remoteOpeningDao: RemoteOpeningDao
}