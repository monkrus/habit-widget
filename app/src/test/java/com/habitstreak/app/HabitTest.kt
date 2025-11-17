package com.habitstreak.app

import com.habitstreak.app.data.Habit
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class HabitTest {

    @Test
    fun `test habit creation with defaults`() {
        val habit = Habit(name = "Test Habit", emoji = "💧")

        assertEquals("Test Habit", habit.name)
        assertEquals("💧", habit.emoji)
        assertEquals(0, habit.currentStreak)
        assertEquals(0, habit.longestStreak)
        assertFalse(habit.isCompletedToday)
        assertTrue(habit.completedDates.isEmpty())
    }

    @Test
    fun `test toggle today marks habit as complete`() {
        val habit = Habit(name = "Water", emoji = "💧")
        val updated = habit.toggleToday()

        assertTrue(updated.isCompletedToday)
        assertEquals(1, updated.currentStreak)
        assertEquals(1, updated.longestStreak)
    }

    @Test
    fun `test toggle today twice removes completion`() {
        val habit = Habit(name = "Water", emoji = "💧")
        val completed = habit.toggleToday()
        val uncompleted = completed.toggleToday()

        assertFalse(uncompleted.isCompletedToday)
        assertEquals(0, uncompleted.currentStreak)
    }

    @Test
    fun `test current streak with consecutive days`() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        val habit = Habit(
            name = "Water",
            emoji = "💧",
            completedDates = listOf(twoDaysAgo, yesterday, today)
        )

        assertEquals(3, habit.currentStreak)
        assertEquals(3, habit.longestStreak)
    }

    @Test
    fun `test current streak breaks with gap`() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val threeDaysAgo = today.minusDays(3)

        val habit = Habit(
            name = "Water",
            emoji = "💧",
            completedDates = listOf(threeDaysAgo, yesterday, today)
        )

        assertEquals(2, habit.currentStreak) // Only yesterday and today count
    }

    @Test
    fun `test current streak is maintained if completed yesterday`() {
        val yesterday = LocalDate.now().minusDays(1)
        val twoDaysAgo = yesterday.minusDays(1)

        val habit = Habit(
            name = "Water",
            emoji = "💧",
            completedDates = listOf(twoDaysAgo, yesterday)
        )

        assertEquals(2, habit.currentStreak) // Streak continues if completed yesterday
    }

    @Test
    fun `test current streak resets if not done today or yesterday`() {
        val threeDaysAgo = LocalDate.now().minusDays(3)
        val fourDaysAgo = LocalDate.now().minusDays(4)

        val habit = Habit(
            name = "Water",
            emoji = "💧",
            completedDates = listOf(fourDaysAgo, threeDaysAgo)
        )

        assertEquals(0, habit.currentStreak) // Streak broken
    }

    @Test
    fun `test longest streak calculation`() {
        val today = LocalDate.now()

        val habit = Habit(
            name = "Water",
            emoji = "💧",
            completedDates = listOf(
                today.minusDays(10),
                today.minusDays(9),
                today.minusDays(8),  // 3-day streak
                today.minusDays(6),
                today.minusDays(5),
                today.minusDays(4),
                today.minusDays(3),  // 4-day streak (longest)
                today.minusDays(1),
                today  // 2-day streak
            )
        )

        assertEquals(4, habit.longestStreak)
        assertEquals(2, habit.currentStreak)
    }

    @Test
    fun `test longest streak with single day`() {
        val today = LocalDate.now()

        val habit = Habit(
            name = "Water",
            emoji = "💧",
            completedDates = listOf(today)
        )

        assertEquals(1, habit.longestStreak)
        assertEquals(1, habit.currentStreak)
    }

    @Test
    fun `test habit with no completions`() {
        val habit = Habit(
            name = "Water",
            emoji = "💧",
            completedDates = emptyList()
        )

        assertEquals(0, habit.currentStreak)
        assertEquals(0, habit.longestStreak)
        assertFalse(habit.isCompletedToday)
    }

    @Test
    fun `test isCompletedToday returns true when completed today`() {
        val today = LocalDate.now()
        val habit = Habit(
            name = "Water",
            emoji = "💧",
            completedDates = listOf(today)
        )

        assertTrue(habit.isCompletedToday)
    }

    @Test
    fun `test isCompletedToday returns false when not completed today`() {
        val yesterday = LocalDate.now().minusDays(1)
        val habit = Habit(
            name = "Water",
            emoji = "💧",
            completedDates = listOf(yesterday)
        )

        assertFalse(habit.isCompletedToday)
    }

    @Test
    fun `test longest streak with multiple equal streaks`() {
        val today = LocalDate.now()

        val habit = Habit(
            name = "Water",
            emoji = "💧",
            completedDates = listOf(
                today.minusDays(8),
                today.minusDays(7),
                today.minusDays(6),  // 3-day streak
                today.minusDays(3),
                today.minusDays(2),
                today.minusDays(1)   // 3-day streak (equal)
            )
        )

        assertEquals(3, habit.longestStreak)
    }

    @Test
    fun `test streak calculation with unordered dates`() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        // Dates in random order
        val habit = Habit(
            name = "Water",
            emoji = "💧",
            completedDates = listOf(today, twoDaysAgo, yesterday)
        )

        assertEquals(3, habit.currentStreak)
        assertEquals(3, habit.longestStreak)
    }
}
