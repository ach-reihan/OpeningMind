package com.openingmind.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    private val LANGUAGE = stringPreferencesKey("language")
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    private val LAST_LOCAL_REPERTOIRE = stringPreferencesKey("last_local_repertoire")
    private val LAST_DICTIONARY_OPENING = stringPreferencesKey("last_dictionary_opening")
    private val LAST_AI_ADVICE = stringPreferencesKey("last_ai_advice")

    val isDarkMode: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE]
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE] ?: "in"
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    val lastLocalRepertoire: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_LOCAL_REPERTOIRE]
    }

    val lastDictionaryOpening: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_DICTIONARY_OPENING]
    }

    val lastAiAdvice: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_AI_ADVICE]
    }

    suspend fun saveDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    suspend fun saveLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = lang
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun saveLastLocalRepertoire(name: String) {
        context.dataStore.edit { it[LAST_LOCAL_REPERTOIRE] = name }
    }

    suspend fun saveLastDictionaryOpening(name: String) {
        context.dataStore.edit { it[LAST_DICTIONARY_OPENING] = name }
    }

    suspend fun saveLastAiAdvice(advice: String) {
        context.dataStore.edit { it[LAST_AI_ADVICE] = advice }
    }
}
