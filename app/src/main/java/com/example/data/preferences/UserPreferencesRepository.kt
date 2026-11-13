package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.yusufDataStore: DataStore<Preferences> by preferencesDataStore(name = "yusuf_electronics_prefs")

enum class AppLanguage(val code: String) {
    BANGLA("bn"),
    ENGLISH("en");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            if (code == ENGLISH.code) ENGLISH else BANGLA
    }
}

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val languageFlow: Flow<AppLanguage> = context.yusufDataStore.data
        .catch { err ->
            if (err is IOException) emit(emptyPreferences()) else throw err
        }
        .map { prefs ->
            AppLanguage.fromCode(prefs[Keys.LANGUAGE])
        }

    val notificationsEnabledFlow: Flow<Boolean> = context.yusufDataStore.data
        .catch { err ->
            if (err is IOException) emit(emptyPreferences()) else throw err
        }
        .map { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setLanguage(language: AppLanguage) {
        context.yusufDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.yusufDataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
