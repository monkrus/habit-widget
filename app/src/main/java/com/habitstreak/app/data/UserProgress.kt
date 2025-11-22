package com.habitstreak.app.data

/**
 * User progress and leveling system
 */
data class UserProgress(
    val totalXp: Int = 0,
    val level: Int = 1,
    val title: String = "Beginner"
) {
    /**
     * XP required for the current level
     */
    val xpForCurrentLevel: Int
        get() = getXpForLevel(level)

    /**
     * XP required for the next level
     */
    val xpForNextLevel: Int
        get() = getXpForLevel(level + 1)

    /**
     * XP progress within current level (0.0 to 1.0)
     */
    val levelProgress: Float
        get() {
            val currentLevelXp = xpForCurrentLevel
            val nextLevelXp = xpForNextLevel
            val xpInLevel = totalXp - currentLevelXp
            val xpNeeded = nextLevelXp - currentLevelXp
            return (xpInLevel.toFloat() / xpNeeded).coerceIn(0f, 1f)
        }

    /**
     * XP remaining until next level
     */
    val xpToNextLevel: Int
        get() = xpForNextLevel - totalXp

    companion object {
        // XP rewards
        const val XP_HABIT_COMPLETE = 10
        const val XP_STREAK_BONUS_PER_DAY = 2 // Bonus XP per streak day
        const val XP_MILESTONE_7 = 50
        const val XP_MILESTONE_30 = 200
        const val XP_MILESTONE_100 = 500
        const val XP_PERFECT_DAY = 30
        const val XP_ACHIEVEMENT_UNLOCK = 25

        // Level thresholds (cumulative XP)
        private val levelThresholds = listOf(
            0,      // Level 1
            100,    // Level 2
            250,    // Level 3
            500,    // Level 4
            850,    // Level 5
            1300,   // Level 6
            1900,   // Level 7
            2600,   // Level 8
            3500,   // Level 9
            4600,   // Level 10
            5900,   // Level 11
            7400,   // Level 12
            9200,   // Level 13
            11300,  // Level 14
            13700,  // Level 15
            16500,  // Level 16
            19700,  // Level 17
            23400,  // Level 18
            27600,  // Level 19
            32400,  // Level 20
            37800,  // Level 21
            44000,  // Level 22
            51000,  // Level 23
            59000,  // Level 24
            68000,  // Level 25
            78000,  // Level 26
            90000,  // Level 27
            104000, // Level 28
            120000, // Level 29
            140000  // Level 30
        )

        // Level titles
        private val levelTitles = mapOf(
            1 to "Beginner",
            2 to "Novice",
            3 to "Apprentice",
            4 to "Student",
            5 to "Practitioner",
            6 to "Skilled",
            7 to "Proficient",
            8 to "Adept",
            9 to "Expert",
            10 to "Veteran",
            11 to "Elite",
            12 to "Master",
            13 to "Grandmaster",
            14 to "Champion",
            15 to "Hero",
            16 to "Legend",
            17 to "Myth",
            18 to "Titan",
            19 to "Immortal",
            20 to "Transcendent",
            21 to "Ascendant",
            22 to "Divine",
            23 to "Celestial",
            24 to "Cosmic",
            25 to "Infinite",
            26 to "Eternal",
            27 to "Omniscient",
            28 to "Omnipotent",
            29 to "Supreme",
            30 to "Ultimate"
        )

        /**
         * Get cumulative XP required for a level
         */
        fun getXpForLevel(level: Int): Int {
            return if (level <= levelThresholds.size) {
                levelThresholds.getOrElse(level - 1) { levelThresholds.last() }
            } else {
                // For levels beyond 30, increase by 25000 per level
                levelThresholds.last() + (level - 30) * 25000
            }
        }

        /**
         * Get level from total XP
         */
        fun getLevelFromXp(totalXp: Int): Int {
            var level = 1
            for (i in levelThresholds.indices) {
                if (totalXp >= levelThresholds[i]) {
                    level = i + 1
                } else {
                    break
                }
            }
            // Handle levels beyond 30
            if (totalXp > levelThresholds.last()) {
                level = 30 + ((totalXp - levelThresholds.last()) / 25000)
            }
            return level
        }

        /**
         * Get title for a level
         */
        fun getTitleForLevel(level: Int): String {
            return if (level <= 30) {
                levelTitles[level] ?: "Ultimate"
            } else {
                "Ultimate ${level - 29}" // Ultimate 2, Ultimate 3, etc.
            }
        }

        /**
         * Create UserProgress from total XP
         */
        fun fromTotalXp(totalXp: Int): UserProgress {
            val level = getLevelFromXp(totalXp)
            return UserProgress(
                totalXp = totalXp,
                level = level,
                title = getTitleForLevel(level)
            )
        }

        /**
         * Calculate XP earned for completing a habit
         */
        fun calculateCompletionXp(currentStreak: Int): Int {
            var xp = XP_HABIT_COMPLETE

            // Streak bonus (capped at 50 bonus XP)
            xp += minOf(currentStreak * XP_STREAK_BONUS_PER_DAY, 50)

            // Milestone bonuses
            when (currentStreak) {
                7 -> xp += XP_MILESTONE_7
                30 -> xp += XP_MILESTONE_30
                100 -> xp += XP_MILESTONE_100
            }

            return xp
        }
    }
}

/**
 * Level up event for UI notification
 */
data class LevelUpEvent(
    val oldLevel: Int,
    val newLevel: Int,
    val newTitle: String
)
