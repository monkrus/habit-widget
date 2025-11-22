package com.habitstreak.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.habitstreak.app.data.Habit
import com.habitstreak.app.data.HabitRepository
import com.habitstreak.app.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Manages backup and restore of habit data
 */
class BackupManager(private val context: Context) {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val repository = HabitRepository(context)
    private val preferencesManager = PreferencesManager(context)

    /**
     * Backup data model
     */
    data class BackupData(
        val version: Int = 1,
        val createdAt: String,
        val habits: List<HabitBackup>,
        val totalXp: Int,
        val unlockedAchievements: List<String>
    )

    data class HabitBackup(
        val id: String,
        val name: String,
        val emoji: String,
        val createdAt: String,
        val completedDates: List<String>,
        val freezeUsedDates: List<String>,
        val position: Int,
        val identity: String?,
        val timeOfDay: String,
        val category: String = "OTHER"
    )

    /**
     * Create a backup file and return its URI for sharing
     */
    suspend fun createBackup(): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val habits = repository.getHabits()
            val totalXp = preferencesManager.getTotalXp()
            val achievements = preferencesManager.getUnlockedAchievementIds()

            val backupData = BackupData(
                version = 1,
                createdAt = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                habits = habits.map { habit ->
                    HabitBackup(
                        id = habit.id,
                        name = habit.name,
                        emoji = habit.emoji,
                        createdAt = habit.createdAt.format(DateTimeFormatter.ISO_DATE),
                        completedDates = habit.completedDates.map { it.format(DateTimeFormatter.ISO_DATE) },
                        freezeUsedDates = habit.freezeUsedDates.map { it.format(DateTimeFormatter.ISO_DATE) },
                        position = habit.position,
                        identity = habit.identity,
                        timeOfDay = habit.timeOfDay.name,
                        category = habit.category.name
                    )
                },
                totalXp = totalXp,
                unlockedAchievements = achievements
            )

            val json = gson.toJson(backupData)
            val fileName = "habit_streak_backup_${LocalDate.now()}.json"
            val backupFile = File(context.cacheDir, fileName)
            backupFile.writeText(json)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create share intent for backup file
     */
    fun createShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Habit Streak Backup")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Restore data from a backup file URI
     */
    suspend fun restoreBackup(uri: Uri): Result<RestoreResult> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Could not open file"))

            val json = inputStream.bufferedReader().use { it.readText() }
            val backupData = gson.fromJson(json, BackupData::class.java)

            // Validate backup version
            if (backupData.version > 1) {
                return@withContext Result.failure(Exception("Backup version not supported. Please update the app."))
            }

            var habitsRestored = 0
            var habitsSkipped = 0

            // Restore habits
            for (habitBackup in backupData.habits) {
                try {
                    val habit = Habit(
                        id = habitBackup.id,
                        name = habitBackup.name,
                        emoji = habitBackup.emoji,
                        createdAt = LocalDate.parse(habitBackup.createdAt),
                        completedDates = habitBackup.completedDates.map { LocalDate.parse(it) },
                        freezeUsedDates = habitBackup.freezeUsedDates.map { LocalDate.parse(it) },
                        position = habitBackup.position,
                        identity = habitBackup.identity,
                        timeOfDay = try {
                            com.habitstreak.app.data.TimeOfDay.valueOf(habitBackup.timeOfDay)
                        } catch (e: Exception) {
                            com.habitstreak.app.data.TimeOfDay.ANYTIME
                        },
                        category = try {
                            com.habitstreak.app.data.HabitCategory.valueOf(habitBackup.category)
                        } catch (e: Exception) {
                            com.habitstreak.app.data.HabitCategory.OTHER
                        }
                    )

                    // Check if habit already exists
                    val existingHabits = repository.getHabits()
                    val exists = existingHabits.any { it.id == habit.id || it.name == habit.name }

                    if (!exists) {
                        repository.addHabit(habit)
                        habitsRestored++
                    } else {
                        habitsSkipped++
                    }
                } catch (e: Exception) {
                    habitsSkipped++
                }
            }

            // Restore XP (only if higher than current)
            val currentXp = preferencesManager.getTotalXp()
            if (backupData.totalXp > currentXp) {
                preferencesManager.addXp(backupData.totalXp - currentXp)
            }

            // Restore achievements
            for (achievementId in backupData.unlockedAchievements) {
                preferencesManager.unlockAchievement(achievementId)
            }

            Result.success(RestoreResult(
                habitsRestored = habitsRestored,
                habitsSkipped = habitsSkipped,
                achievementsRestored = backupData.unlockedAchievements.size
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class RestoreResult(
        val habitsRestored: Int,
        val habitsSkipped: Int,
        val achievementsRestored: Int
    )
}
