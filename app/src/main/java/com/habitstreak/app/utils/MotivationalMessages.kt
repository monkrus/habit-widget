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

    // ===== Identity-Based Messages =====

    /**
     * Common identity suggestions for habits
     */
    val identitySuggestions = mapOf(
        "Exercise" to "Athlete",
        "Running" to "Runner",
        "Workout" to "Athlete",
        "Gym" to "Athlete",
        "Read" to "Reader",
        "Reading" to "Reader",
        "Meditate" to "Meditator",
        "Meditation" to "Meditator",
        "Write" to "Writer",
        "Writing" to "Writer",
        "Journal" to "Writer",
        "Code" to "Developer",
        "Coding" to "Developer",
        "Programming" to "Developer",
        "Learn" to "Learner",
        "Study" to "Scholar",
        "Practice" to "Practitioner",
        "Walk" to "Walker",
        "Yoga" to "Yogi",
        "Stretch" to "Athlete",
        "Sleep" to "Wellness Champion",
        "Water" to "Health Advocate",
        "Hydrate" to "Health Advocate",
        "Healthy" to "Health Champion",
        "Cook" to "Chef",
        "Cooking" to "Chef",
        "Art" to "Artist",
        "Draw" to "Artist",
        "Paint" to "Artist",
        "Music" to "Musician",
        "Guitar" to "Musician",
        "Piano" to "Musician",
        "Language" to "Polyglot",
        "Spanish" to "Linguist",
        "French" to "Linguist"
    )

    /**
     * Get a suggested identity based on habit name
     */
    fun suggestIdentity(habitName: String): String? {
        val lowerName = habitName.lowercase()
        return identitySuggestions.entries.find { (key, _) ->
            lowerName.contains(key.lowercase())
        }?.value
    }

    /**
     * Identity proof messages - shown when completing habits
     * Format: "Proof you are a {identity}!" or similar
     */
    private val identityProofMessages = listOf(
        "Proof you're a %s! 🎯",
        "That's what %ss do! 💪",
        "A true %s at work! ⚡",
        "%ss don't skip days! 🔥",
        "Living the %s life! ✨",
        "This is who you are: a %s! 🌟",
        "%s status: CONFIRMED! ✅",
        "Another win for team %s! 🏆"
    )

    /**
     * Identity reinforcement messages for streak milestones
     */
    private val identityMilestoneMessages = mapOf(
        7 to listOf(
            "7 days! You're becoming a real %s! 🔥",
            "A week of being a %s! It's becoming who you are! 💪"
        ),
        30 to listOf(
            "30 days! Being a %s is now part of your DNA! 🧬",
            "A month of %s life! You ARE a %s now! 👑"
        ),
        100 to listOf(
            "100 days! %s isn't what you do, it's WHO you are! 💎",
            "Century as a %s! Legendary identity achieved! 🏆"
        )
    )

    /**
     * Get an identity-based message for habit completion
     */
    fun getIdentityMessage(identity: String?, streak: Int): String? {
        if (identity.isNullOrBlank()) return null

        // Check for milestone messages first
        val milestoneMsg = identityMilestoneMessages[streak]?.random()
        if (milestoneMsg != null) {
            return milestoneMsg.replace("%s", identity)
        }

        // Return regular identity proof message
        return identityProofMessages.random().replace("%s", identity)
    }

    /**
     * Get identity badge text for display
     */
    fun getIdentityBadge(identity: String, streak: Int): String {
        return when {
            streak >= 100 -> "Master $identity"
            streak >= 30 -> "Dedicated $identity"
            streak >= 7 -> "Growing $identity"
            else -> "Aspiring $identity"
        }
    }
}
