package com.habitstreak.app.utils

import kotlin.random.Random

/**
 * Provides motivational messages for habit completion
 */
object MotivationalMessages {

    private val completionMessages = listOf(
        "Amazing! Keep it up! 🎉",
        "You're crushing it! 💪",
        "Streak building! 🔥",
        "Unstoppable! ⚡",
        "Great work! ✨",
        "You're on fire! 🔥",
        "Killing it! 🎯",
        "Legendary! 🏆",
        "Fantastic! 🌟",
        "You got this! 💯",
        "Nailed it! ✅",
        "Perfect! 🎊",
        "Outstanding! 🌈",
        "Brilliant! 💎",
        "Superb! 🚀",
        "Excellent! ⭐"
    )

    private val milestoneMessages = mapOf(
        7 to listOf(
            "7 days strong! You're building real habits! 🔥",
            "One week streak! That's commitment! 💪",
            "7 days in a row! Amazing! 🎉"
        ),
        14 to listOf(
            "2 weeks! You're unstoppable! 🚀",
            "14 days! Habits are forming! ⚡",
            "Two weeks strong! Incredible! 🌟"
        ),
        30 to listOf(
            "30 DAYS! This is now a lifestyle! 🏆",
            "One month! You're a habit master! 👑",
            "30 day streak! Absolutely legendary! 🎊"
        ),
        100 to listOf(
            "100 DAYS! You're in the 1%! 💎",
            "Century club! Absolutely incredible! 🔥",
            "100 day streak! You're unstoppable! 🚀"
        ),
        365 to listOf(
            "ONE YEAR! You are LEGENDARY! 👑",
            "365 days! A full year of commitment! 🏆",
            "1 year streak! You're an absolute CHAMPION! 💯"
        )
    )

    private val firstCompletionMessages = listOf(
        "Great start! First of many! 🌱",
        "Journey of 1000 miles begins with one step! 🚶",
        "Welcome to your new habit! 🎯",
        "First step taken! Keep going! ✨"
    )

    /**
     * Get a motivational message for completing a habit
     */
    fun getMessage(currentStreak: Int, isFirstCompletion: Boolean = false): String {
        return when {
            isFirstCompletion -> firstCompletionMessages.random()
            milestoneMessages.containsKey(currentStreak) -> {
                milestoneMessages[currentStreak]?.random() ?: completionMessages.random()
            }
            else -> completionMessages.random()
        }
    }

    /**
     * Get emoji based on streak length
     */
    fun getStreakEmoji(streak: Int): String {
        return when {
            streak >= 365 -> "👑" // King
            streak >= 100 -> "💎" // Diamond
            streak >= 30 -> "🏆" // Trophy
            streak >= 14 -> "⚡" // Lightning
            streak >= 7 -> "🔥" // Fire
            streak >= 3 -> "✨" // Sparkles
            else -> "🌱" // Seedling
        }
    }

    /**
     * Get fire size based on streak (for visual representation)
     */
    fun getFireSize(streak: Int): Float {
        return when {
            streak >= 100 -> 1.5f
            streak >= 30 -> 1.3f
            streak >= 14 -> 1.2f
            streak >= 7 -> 1.1f
            else -> 1.0f
        }
    }
}
