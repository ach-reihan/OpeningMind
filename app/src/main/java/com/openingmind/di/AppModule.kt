package com.openingmind.di

import android.content.Context
import androidx.room.Room
import com.openingmind.BuildConfig
import com.openingmind.data.local.dao.RepertoireDao
import com.openingmind.data.local.db.AppDatabase
import com.openingmind.data.remote.api.AzureAiService
import com.openingmind.data.remote.api.LichessApiService
import com.openingmind.data.repository.RepertoireRepositoryImpl
import com.openingmind.domain.repository.RepertoireRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ==========================================
    // 1. DATABASE PROVIDERS (Room)
    // ==========================================

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "openingmind_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideRepertoireDao(db: AppDatabase): RepertoireDao {
        return db.repertoireDao
    }

    @Provides
    @Singleton
    fun provideRemoteOpeningDao(db: AppDatabase): com.openingmind.data.local.dao.RemoteOpeningDao {
        return db.remoteOpeningDao
    }

    // ==========================================
    // 2. NETWORK PROVIDERS (Retrofit)
    // ==========================================

    @Provides
    @Singleton
    fun provideLichessApiService(): LichessApiService {
        return Retrofit.Builder()
            .baseUrl("https://lichess.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LichessApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAzureAiService(): AzureAiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.AZURE_AI_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AzureAiService::class.java)
    }

    // ==========================================
    // 3. REPOSITORY BINDINGS (Clean Architecture Bridge)
    // ==========================================

    @Provides
    @Singleton
    fun provideRepertoireRepository(
        dao: RepertoireDao,
        remoteOpeningDao: com.openingmind.data.local.dao.RemoteOpeningDao,
        lichessApi: LichessApiService,
        azureApi: AzureAiService,
        @ApplicationContext context: Context
    ): RepertoireRepository {
        return RepertoireRepositoryImpl(dao, remoteOpeningDao, lichessApi, azureApi, context)
    }
}