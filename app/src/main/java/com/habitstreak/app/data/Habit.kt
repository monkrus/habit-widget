package com.habitstreak.app.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Habit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val createdAt: LocalDate = LocalDate.now(),
    val completedDates: List<LocalDate> = emptyList(),
    val freezeUsedDates: List<LocalDate> = emptyList(),
    val position: Int = 0,
    val identity: String? = null // e.g., "Runner", "Reader", "Meditator"
) {
    val currentStreak: Int
        get() = calculateCurrentStreak()

    val longestStreak: Int
        get() = calculateLongestStreak()

    val isCompletedToday: Boolean
        get() = completedDates.contains(LocalDate.now())

    val isFrozenToday: Boolean
        get() = freezeUsedDates.contains(LocalDate.now())

    /**
     * Calculate current streak, respecting freeze days.
     * Freeze days count as "protected" days that don't break the streak.
     */
    private fun calculateCurrentStreak(): Int {
        val today = LocalDate.now()
        val allProtectedDates = (completedDates + freezeUsedDates).toSet()

        if (allProtectedDates.isEmpty()) return 0

        val sortedDates = allProtectedDates.sortedDescending()

        // Check if completed/frozen today or yesterday (to maintain streak)
        val startDate = when {
            sortedDates.first() == today -> today
            sortedDates.first() == today.minusDays(1) -> today.minusDays(1)
            else -> return 0 // Streak broken
        }

        var streak = 0
        var currentDate = startDate

        for (date in sortedDates) {
            if (date == currentDate) {
                // Only count actual completions toward streak number
                // Freezes maintain streak but don't add to count
                if (completedDates.contains(date)) {
                    streak++
                }
                currentDate = currentDate.minusDays(1)
            } else if (date < currentDate) {
                // Gap in streak (no completion or freeze)
                break
            }
        }

        return streak
    }

    /**
     * Calculate longest streak, respecting freeze days.
     */
    private fun calculateLongestStreak(): Int {
        val allProtectedDates = (completedDates + freezeUsedDates).toSet()
        if (allProtectedDates.isEmpty()) return 0

        val sortedDates = allProtectedDates.sorted()
        var maxStreak = 0
        var currentStreakCount = 0
        var previousDate: LocalDate? = null

        for (date in sortedDates) {
            if (previousDate == null || ChronoUnit.DAYS.between(previousDate, date) == 1L) {
                // Consecutive day - only count if it's a completion (not freeze)
                if (completedDates.contains(date)) {
                    currentStreakCount++
                }
                // Still maintain streak continuity even on freeze days
            } else {
                // Gap detected - streak broken
                maxStreak = maxOf(maxStreak, currentStreakCount)
                currentStreakCount = if (completedDates.contains(date)) 1 else 0
            }
            previousDate = date
        }

        return maxOf(maxStreak, currentStreakCount)
    }

    fun toggleToday(): Habit {
        val today = LocalDate.now()
        return if (isCompletedToday) {
            copy(completedDates = completedDates.filter { it != today })
        } else {
            // If frozen today, remove freeze when completing
            val newFreezeDates = if (isFrozenToday) {
                freezeUsedDates.filter { it != today }
            } else {
                freezeUsedDates
            }
            copy(
                completedDates = completedDates + today,
                freezeUsedDates = newFreezeDates
            )
        }
    }

    /**
     * Use a freeze for today. Returns updated Habit or null if already completed/frozen.
     */
    fun useFreezeToday(): Habit? {
        val today = LocalDate.now()
        // Can't freeze if already completed or already frozen
        if (isCompletedToday || isFrozenToday) return null
        return copy(freezeUsedDates = freezeUsedDates + today)
    }

    /**
     * Check if streak would be broken without action today.
     * Used to show warning/suggest freeze.
     */
    val isStreakAtRisk: Boolean
        get() {
            if (currentStreak == 0) return false
            val today = LocalDate.now()
            return !isCompletedToday && !isFrozenToday
        }
}
