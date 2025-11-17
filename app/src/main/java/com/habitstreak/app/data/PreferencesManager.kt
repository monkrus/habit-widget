package com.habitstreak.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.prefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    private val gson = Gson()
    private val isProKey = booleanPreferencesKey("is_pro")
    private val reminderEnabledKey = booleanPreferencesKey("reminder_enabled")
    private val reminderHourKey = intPreferencesKey("reminder_hour")
    private val reminderMinuteKey = intPreferencesKey("reminder_minute")
    private val achievementsKey = stringPreferencesKey("achievements")

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

    // Achievement tracking
    data class UnlockedAchievement(
        val achievementId: String,
        val unlockedDate: LocalDate
    )

    val achievementsFlow: Flow<List<Achievement>> = context.prefsDataStore.data.map { preferences ->
        val json = preferences[achievementsKey] ?: "[]"
        val type = object : TypeToken<List<UnlockedAchievement>>() {}.type
        val unlocked: List<UnlockedAchievement> = gson.fromJson(json, type)

        Achievement.ALL.map { achievement ->
            val unlockedData = unlocked.find { it.achievementId == achievement.id }
            achievement.copy(unlockedDate = unlockedData?.unlockedDate)
        }
    }

    suspend fun unlockAchievement(achievementId: String): Boolean {
        val preferences = context.prefsDataStore.data.first()
        val json = preferences[achievementsKey] ?: "[]"
        val type = object : TypeToken<List<UnlockedAchievement>>() {}.type
        val unlocked: MutableList<UnlockedAchievement> = gson.fromJson(json, type)

        // Check if already unlocked
        if (unlocked.any { it.achievementId == achievementId }) {
            return false // Already unlocked
        }

        // Add new achievement
        unlocked.add(UnlockedAchievement(achievementId, LocalDate.now()))

        // Save
        context.prefsDataStore.edit { prefs ->
            prefs[achievementsKey] = gson.toJson(unlocked)
        }

        return true // Newly unlocked
    }

    suspend fun getUnlockedAchievements(): List<Achievement> {
        return try {
            val preferences = context.prefsDataStore.data.first()
            val json = preferences[achievementsKey] ?: "[]"
            val type = object : TypeToken<List<UnlockedAchievement>>() {}.type
            val unlocked: List<UnlockedAchievement> = gson.fromJson(json, type)

            Achievement.ALL.mapNotNull { achievement ->
                val unlockedData = unlocked.find { it.achievementId == achievement.id }
                if (unlockedData != null) {
                    achievement.copy(unlockedDate = unlockedData.unlockedDate)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
