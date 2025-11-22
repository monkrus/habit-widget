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
    private val freezesUsedThisMonthKey = intPreferencesKey("freezes_used_this_month")
    private val freezeMonthKey = intPreferencesKey("freeze_month") // Track which month the count is for
    private val totalFreezesUsedKey = intPreferencesKey("total_freezes_used")
    private val totalXpKey = intPreferencesKey("total_xp")

    companion object {
        const val FREE_MONTHLY_FREEZE_LIMIT = 3
    }

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
        return try {
            val preferences = context.prefsDataStore.data.first()
            preferences[isProKey] ?: false
        } catch (e: Exception) {
            false
        }
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

    // ===== Freeze Tracking =====

    /**
     * Flow of available freezes remaining this month
     */
    val freezesAvailableFlow: Flow<Int> = context.prefsDataStore.data.map { preferences ->
        val isPro = preferences[isProKey] ?: false
        if (isPro) {
            Int.MAX_VALUE // Unlimited for Pro users
        } else {
            val currentMonth = LocalDate.now().monthValue
            val savedMonth = preferences[freezeMonthKey] ?: currentMonth
            val usedThisMonth = if (savedMonth == currentMonth) {
                preferences[freezesUsedThisMonthKey] ?: 0
            } else {
                0 // New month, reset count
            }
            maxOf(0, FREE_MONTHLY_FREEZE_LIMIT - usedThisMonth)
        }
    }

    /**
     * Get remaining freezes for this month
     */
    suspend fun getFreezesAvailable(): Int {
        return try {
            val preferences = context.prefsDataStore.data.first()
            val isPro = preferences[isProKey] ?: false
            if (isPro) {
                Int.MAX_VALUE
            } else {
                val currentMonth = LocalDate.now().monthValue
                val savedMonth = preferences[freezeMonthKey] ?: currentMonth
                val usedThisMonth = if (savedMonth == currentMonth) {
                    preferences[freezesUsedThisMonthKey] ?: 0
                } else {
                    0
                }
                maxOf(0, FREE_MONTHLY_FREEZE_LIMIT - usedThisMonth)
            }
        } catch (e: Exception) {
            FREE_MONTHLY_FREEZE_LIMIT
        }
    }

    /**
     * Use a freeze. Returns true if freeze was available and used, false otherwise.
     */
    suspend fun useFreeze(): Boolean {
        val preferences = context.prefsDataStore.data.first()
        val isPro = preferences[isProKey] ?: false
        val currentMonth = LocalDate.now().monthValue
        val savedMonth = preferences[freezeMonthKey] ?: currentMonth

        // Reset count if new month
        val usedThisMonth = if (savedMonth == currentMonth) {
            preferences[freezesUsedThisMonthKey] ?: 0
        } else {
            0
        }

        // Check if freeze is available (Pro users always have freezes)
        if (!isPro && usedThisMonth >= FREE_MONTHLY_FREEZE_LIMIT) {
            return false // No freezes available
        }

        // Use the freeze
        context.prefsDataStore.edit { prefs ->
            prefs[freezeMonthKey] = currentMonth
            prefs[freezesUsedThisMonthKey] = usedThisMonth + 1
            val totalUsed = (prefs[totalFreezesUsedKey] ?: 0) + 1
            prefs[totalFreezesUsedKey] = totalUsed
        }

        return true
    }

    /**
     * Get total freezes used (for achievements)
     */
    suspend fun getTotalFreezesUsed(): Int {
        return try {
            val preferences = context.prefsDataStore.data.first()
            preferences[totalFreezesUsedKey] ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Get freezes used this month (for UI display)
     */
    suspend fun getFreezesUsedThisMonth(): Int {
        return try {
            val preferences = context.prefsDataStore.data.first()
            val currentMonth = LocalDate.now().monthValue
            val savedMonth = preferences[freezeMonthKey] ?: currentMonth
            if (savedMonth == currentMonth) {
                preferences[freezesUsedThisMonthKey] ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    // ===== XP & Level System =====

    /**
     * Flow of user progress (XP and level)
     */
    val userProgressFlow: Flow<UserProgress> = context.prefsDataStore.data.map { preferences ->
        val totalXp = preferences[totalXpKey] ?: 0
        UserProgress.fromTotalXp(totalXp)
    }

    /**
     * Get current user progress
     */
    suspend fun getUserProgress(): UserProgress {
        return try {
            val preferences = context.prefsDataStore.data.first()
            val totalXp = preferences[totalXpKey] ?: 0
            UserProgress.fromTotalXp(totalXp)
        } catch (e: Exception) {
            UserProgress()
        }
    }

    /**
     * Add XP and return level up event if leveled up
     */
    suspend fun addXp(amount: Int): LevelUpEvent? {
        val preferences = context.prefsDataStore.data.first()
        val oldTotalXp = preferences[totalXpKey] ?: 0
        val newTotalXp = oldTotalXp + amount

        val oldLevel = UserProgress.getLevelFromXp(oldTotalXp)
        val newLevel = UserProgress.getLevelFromXp(newTotalXp)

        context.prefsDataStore.edit { prefs ->
            prefs[totalXpKey] = newTotalXp
        }

        return if (newLevel > oldLevel) {
            LevelUpEvent(
                oldLevel = oldLevel,
                newLevel = newLevel,
                newTitle = UserProgress.getTitleForLevel(newLevel)
            )
        } else {
            null
        }
    }

    /**
     * Get total XP
     */
    suspend fun getTotalXp(): Int {
        return try {
            val preferences = context.prefsDataStore.data.first()
            preferences[totalXpKey] ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
