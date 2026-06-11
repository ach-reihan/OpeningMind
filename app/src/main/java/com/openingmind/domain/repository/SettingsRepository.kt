package com.openingmind.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun isDarkMode(): Flow<Boolean?>
    fun getLanguage(): Flow<String>
    suspend fun setDarkMode(isDark: Boolean)
    suspend fun setLanguage(lang: String)
}
