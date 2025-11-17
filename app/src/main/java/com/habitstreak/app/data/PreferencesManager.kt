package com.habitstreak.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    private val isProKey = booleanPreferencesKey("is_pro")
    private val reminderEnabledKey = booleanPreferencesKey("reminder_enabled")
    private val reminderHourKey = intPreferencesKey("reminder_hour")
    private val reminderMinuteKey = intPreferencesKey("reminder_minute")

    val isProFlow: Flow<Boolean> = context.prefsDataStore.data.map { preferences ->
        preferences[isProKey] ?: false
    }

    val reminderEnabledFlow: Flow<Boolean> = context.prefsDataStore.data.map { preferences ->
        preferences[reminderEnabledKey] ?: true // Default: enabled
    }

    val reminderTimeFlow: Flow<Pair<Int, Int>> = context.prefsDataStore.data.map { preferences ->
        val hour = preferences[reminderHourKey] ?: 20 // Default: 8 PM
        val minute = preferences[reminderMinuteKey] ?: 0
        Pair(hour, minute)
    }

    suspend fun setProVersion(isPro: Boolean) {
        context.prefsDataStore.edit { preferences ->
            preferences[isProKey] = isPro
        }
    }

    suspend fun isPro(): Boolean {
claude/quick-project-ideas-019ZMgnyxFmJhgdf6waV9arf
        return try {
            val preferences = context.prefsDataStore.data.first()
            preferences[isProKey] ?: false
        } catch (e: Exception) {
            false
        }

        return context.prefsDataStore.data.map { preferences ->
            preferences[isProKey] ?: false
        }.first()
 main
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.prefsDataStore.edit { preferences ->
            preferences[reminderEnabledKey] = enabled
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.prefsDataStore.edit { preferences ->
            preferences[reminderHourKey] = hour
            preferences[reminderMinuteKey] = minute
        }
    }

    suspend fun getReminderEnabled(): Boolean {
        return try {
            val preferences = context.prefsDataStore.data.first()
            preferences[reminderEnabledKey] ?: true
        } catch (e: Exception) {
            true
        }
    }

    suspend fun getReminderTime(): Pair<Int, Int> {
        return try {
            val preferences = context.prefsDataStore.data.first()
            val hour = preferences[reminderHourKey] ?: 20
            val minute = preferences[reminderMinuteKey] ?: 0
            Pair(hour, minute)
        } catch (e: Exception) {
            Pair(20, 0)
        }
    }
}
