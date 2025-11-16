package com.habitstreak.app.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Habit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val createdAt: LocalDate = LocalDate.now(),
    val completedDates: List<LocalDate> = emptyList(),
    val position: Int = 0
) {
    val currentStreak: Int
        get() = calculateCurrentStreak()

    val longestStreak: Int
        get() = calculateLongestStreak()

    val isCompletedToday: Boolean
        get() = completedDates.contains(LocalDate.now())

    private fun calculateCurrentStreak(): Int {
        if (completedDates.isEmpty()) return 0

        val today = LocalDate.now()
        val sortedDates = completedDates.sortedDescending()

        // Check if completed today or yesterday (to maintain streak)
        val startDate = if (sortedDates.first() == today) {
            today
        } else if (sortedDates.first() == today.minusDays(1)) {
            today.minusDays(1)
        } else {
            return 0 // Streak broken
        }

        var streak = 0
        var currentDate = startDate

        for (date in sortedDates) {
            if (date == currentDate) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (date < currentDate) {
                // Gap in streak
                break
            }
        }

        return streak
    }

    private fun calculateLongestStreak(): Int {
        if (completedDates.isEmpty()) return 0

        val sortedDates = completedDates.sorted()
        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until sortedDates.size) {
            val daysBetween = ChronoUnit.DAYS.between(sortedDates[i - 1], sortedDates[i])
            if (daysBetween == 1L) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        return maxStreak
    }

    fun toggleToday(): Habit {
        val today = LocalDate.now()
        return if (isCompletedToday) {
            copy(completedDates = completedDates.filter { it != today })
        } else {
            copy(completedDates = completedDates + today)
        }
    }
}
