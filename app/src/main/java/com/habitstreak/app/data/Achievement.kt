package com.habitstreak.app.data

import java.time.LocalDate

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val unlockedDate: LocalDate? = null
) {
    val isUnlocked: Boolean get() = unlockedDate != null

    companion object {
        // Streak achievements
        val FIRST_STEP = Achievement(
            id = "first_step",
            title = "First Step",
            description = "Complete a habit for the first time",
            emoji = "🎯"
        )

        val WEEK_WARRIOR = Achievement(
            id = "week_warrior",
            title = "Week Warrior",
            description = "Complete a habit for 7 days in a row",
            emoji = "🔥"
        )

        val MONTH_MASTER = Achievement(
            id = "month_master",
            title = "Month Master",
            description = "Complete a habit for 30 days in a row",
            emoji = "⭐"
        )

        val CENTURY_CLUB = Achievement(
            id = "century_club",
            title = "Century Club",
            description = "Complete a habit for 100 days in a row",
            emoji = "💎"
        )

        val YEAR_LEGEND = Achievement(
            id = "year_legend",
            title = "Year Legend",
            description = "Complete a habit for 365 days in a row",
            emoji = "🏆"
        )

        // Completion achievements
        val PERFECT_WEEK = Achievement(
            id = "perfect_week",
            title = "Perfect Week",
            description = "Complete all habits for 7 days straight",
            emoji = "🎯"
        )

        val PERFECT_MONTH = Achievement(
            id = "perfect_month",
            title = "Perfect Month",
            description = "Complete all habits for 30 days straight",
            emoji = "🌟"
        )

        // Habit count achievements
        val GETTING_STARTED = Achievement(
            id = "getting_started",
            title = "Getting Started",
            description = "Create your first habit",
            emoji = "🌱"
        )

        val HABIT_COLLECTOR = Achievement(
            id = "habit_collector",
            title = "Habit Collector",
            description = "Create 3 habits",
            emoji = "📚"
        )

        val HABIT_MASTER = Achievement(
            id = "habit_master",
            title = "Habit Master",
            description = "Create 5 habits",
            emoji = "👑"
        )

        // Consistency achievements
        val COMEBACK_KID = Achievement(
            id = "comeback_kid",
            title = "Comeback Kid",
            description = "Restart a habit after a break",
            emoji = "💪"
        )

        val EARLY_BIRD = Achievement(
            id = "early_bird",
            title = "Early Bird",
            description = "Complete a habit before 8am",
            emoji = "🌅"
        )

        val NIGHT_OWL = Achievement(
            id = "night_owl",
            title = "Night Owl",
            description = "Complete a habit after 10pm",
            emoji = "🦉"
        )

        // All achievements
        val ALL = listOf(
            GETTING_STARTED,
            FIRST_STEP,
            WEEK_WARRIOR,
            HABIT_COLLECTOR,
            PERFECT_WEEK,
            MONTH_MASTER,
            HABIT_MASTER,
            PERFECT_MONTH,
            CENTURY_CLUB,
            YEAR_LEGEND,
            COMEBACK_KID,
            EARLY_BIRD,
            NIGHT_OWL
        )

        fun getById(id: String): Achievement? {
            return ALL.find { it.id == id }
        }
    }
}

/**
 * Check which achievements should be unlocked based on current state
 */
object AchievementChecker {

    fun checkStreakAchievements(streak: Int): List<Achievement> {
        val achievements = mutableListOf<Achievement>()

        when (streak) {
            1 -> achievements.add(Achievement.FIRST_STEP)
            7 -> achievements.add(Achievement.WEEK_WARRIOR)
            30 -> achievements.add(Achievement.MONTH_MASTER)
            100 -> achievements.add(Achievement.CENTURY_CLUB)
            365 -> achievements.add(Achievement.YEAR_LEGEND)
        }

        return achievements
    }

    fun checkHabitCountAchievements(habitCount: Int): List<Achievement> {
        val achievements = mutableListOf<Achievement>()

        when (habitCount) {
            1 -> achievements.add(Achievement.GETTING_STARTED)
            3 -> achievements.add(Achievement.HABIT_COLLECTOR)
            5 -> achievements.add(Achievement.HABIT_MASTER)
        }

        return achievements
    }

    fun checkPerfectWeekAchievement(habits: List<Habit>): Boolean {
        if (habits.isEmpty()) return false

        // Check if all habits have been completed for the last 7 days
        val today = LocalDate.now()
        return (0..6).all { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            habits.all { habit -> habit.completedDates.contains(date) }
        }
    }

    fun checkPerfectMonthAchievement(habits: List<Habit>): Boolean {
        if (habits.isEmpty()) return false

        // Check if all habits have been completed for the last 30 days
        val today = LocalDate.now()
        return (0..29).all { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            habits.all { habit -> habit.completedDates.contains(date) }
        }
    }

    /**
     * Check if user has earned the Comeback Kid achievement
     * This is awarded when a habit is restarted after breaking a streak
     */
    fun checkComebackKidAchievement(habit: Habit): Boolean {
        // If current streak is positive and longest streak is greater, they came back
        return habit.currentStreak > 0 && habit.longestStreak > habit.currentStreak
    }
}
