package com.openingmind.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.openingmind.data.local.dao.RepertoireDao
import com.openingmind.data.local.entity.RepertoireEntity

@Database(entities = [RepertoireEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val repertoireDao: RepertoireDao
}