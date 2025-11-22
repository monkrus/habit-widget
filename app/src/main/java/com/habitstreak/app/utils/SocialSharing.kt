package com.habitstreak.app.utils

import android.content.Context
import android.content.Intent
import com.habitstreak.app.data.Achievement
import com.habitstreak.app.data.Habit
import com.habitstreak.app.data.UserProgress

/**
 * Utility for sharing achievements and progress on social media
 */
object SocialSharing {

    /**
     * Share an unlocked achievement
     */
    fun shareAchievement(context: Context, achievement: Achievement) {
        val message = buildString {
            append("${achievement.emoji} Achievement Unlocked!\n\n")
            append("\"${achievement.title}\"\n")
            append("${achievement.description}\n\n")
            append("#HabitStreak #BuildingHabits #Achievement")
        }
        shareText(context, message, "Share Achievement")
    }

    /**
     * Share a streak milestone
     */
    fun shareStreakMilestone(context: Context, habit: Habit) {
        val message = buildString {
            append("${habit.emoji} ${habit.currentStreak} Day Streak!\n\n")
            append("I've completed \"${habit.name}\" for ${habit.currentStreak} days in a row!\n\n")
            if (habit.identity != null) {
                append("Becoming a ${habit.identity}, one day at a time.\n\n")
            }
            append("#HabitStreak #${habit.currentStreak}DayStreak #BuildingHabits")
        }
        shareText(context, message, "Share Streak")
    }

    /**
     * Share level up achievement
     */
    fun shareLevelUp(context: Context, level: Int, title: String) {
        val message = buildString {
            append("Level Up! Reached Level $level\n\n")
            append("New Title: \"$title\"\n\n")
            append("Building better habits, one day at a time!\n\n")
            append("#HabitStreak #Level$level #PersonalGrowth")
        }
        shareText(context, message, "Share Level Up")
    }

    /**
     * Share weekly progress
     */
    fun shareWeeklyProgress(
        context: Context,
        totalCompletions: Int,
        perfectDays: Int,
        completionRate: Int
    ) {
        val message = buildString {
            append("My Week in Habits\n\n")
            append("$totalCompletions completions\n")
            append("$perfectDays perfect days\n")
            append("$completionRate% completion rate\n\n")
            append("#HabitStreak #WeeklyProgress #BuildingHabits")
        }
        shareText(context, message, "Share Weekly Progress")
    }

    /**
     * Share overall progress
     */
    fun shareProgress(context: Context, userProgress: UserProgress, habitCount: Int) {
        val message = buildString {
            append("My Habit Streak Journey\n\n")
            append("Level ${userProgress.level}: ${userProgress.title}\n")
            append("${userProgress.totalXp} XP earned\n")
            append("$habitCount habits being tracked\n\n")
            append("Building better habits every day!\n\n")
            append("#HabitStreak #PersonalGrowth #Habits")
        }
        shareText(context, message, "Share Progress")
    }

    private fun shareText(context: Context, text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
