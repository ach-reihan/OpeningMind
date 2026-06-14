package com.openingmind.data.repository

import com.openingmind.data.local.UserPreferences
import com.openingmind.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : SettingsRepository {
    override fun isDarkMode(): Flow<Boolean?> = userPreferences.isDarkMode
    override fun getLanguage(): Flow<String> = userPreferences.language
    override fun isOnboardingCompleted(): Flow<Boolean> = userPreferences.onboardingCompleted
    override suspend fun setDarkMode(isDark: Boolean) = userPreferences.saveDarkMode(isDark)
    override suspend fun setLanguage(lang: String) = userPreferences.saveLanguage(lang)
    override suspend fun setOnboardingCompleted(completed: Boolean) = userPreferences.setOnboardingCompleted(completed)
}
