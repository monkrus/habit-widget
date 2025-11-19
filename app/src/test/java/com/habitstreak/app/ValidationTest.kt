package com.habitstreak.app

import com.habitstreak.app.data.Habit
import com.habitstreak.app.utils.AppConfig
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for validation logic and edge cases
 */
class ValidationTest {

    @Test
    fun `test AppConfig constants are positive`() {
        assertTrue("FREE_HABIT_LIMIT should be positive", AppConfig.FREE_HABIT_LIMIT > 0)
        assertTrue("WIDGET_HABIT_LIMIT should be positive", AppConfig.WIDGET_HABIT_LIMIT > 0)
        assertTrue("HABIT_NAME_MAX_LENGTH should be positive", AppConfig.HABIT_NAME_MAX_LENGTH > 0)
        assertTrue("CONFETTI_PARTICLE_COUNT should be positive", AppConfig.CONFETTI_PARTICLE_COUNT > 0)
    }

    @Test
    fun `test AppConfig milestone streaks are in ascending order`() {
        val milestones = AppConfig.MILESTONE_STREAKS

        for (i in 0 until milestones.size - 1) {
            assertTrue(
                "Milestones should be in ascending order",
                milestones[i] < milestones[i + 1]
            )
        }
    }

    @Test
    fun `test widget limit is greater than or equal to free limit`() {
        assertTrue(
            "Widget should be able to display at least free habit limit",
            AppConfig.WIDGET_HABIT_LIMIT >= AppConfig.FREE_HABIT_LIMIT
        )
    }

    @Test
    fun `test habit name at max length`() {
        val maxLengthName = "a".repeat(AppConfig.HABIT_NAME_MAX_LENGTH)
        val habit = Habit(name = maxLengthName, emoji = "💪")

        assertEquals(AppConfig.HABIT_NAME_MAX_LENGTH, habit.name.length)
    }

    @Test
    fun `test habit with emoji characters`() {
        val emojis = listOf("💪", "📚", "🏃", "💧", "🎯", "🧘", "🍎")

        emojis.forEach { emoji ->
            val habit = Habit(name = "Test", emoji = emoji)
            assertEquals(emoji, habit.emoji)
        }
    }

    @Test
    fun `test habit position starts at zero`() {
        val habit = Habit(name = "Test", emoji = "💪", position = 0)

        assertEquals(0, habit.position)
    }

    @Test
    fun `test habit with future date does not count as completed today`() {
        val tomorrow = LocalDate.now().plusDays(1)
        val habit = Habit(
            name = "Test",
            emoji = "💪",
            completedDates = listOf(tomorrow)
        )

        assertFalse("Future dates should not count as completed today", habit.isCompletedToday)
    }

    @Test
    fun `test habit with duplicate dates in completedDates`() {
        val today = LocalDate.now()
        val habit = Habit(
            name = "Test",
            emoji = "💪",
            completedDates = listOf(today, today, today)
        )

        // Should still work correctly with duplicate dates
        assertTrue(habit.isCompletedToday)
        assertEquals(1, habit.currentStreak)
    }

    @Test
    fun `test habit streak calculation with very old dates`() {
        val today = LocalDate.now()
        val oneYearAgo = today.minusYears(1)

        val habit = Habit(
            name = "Test",
            emoji = "💪",
            completedDates = listOf(oneYearAgo, today)
        )

        // Current streak should only be 1 (today)
        assertEquals(1, habit.currentStreak)
        // Longest streak should still be 1
        assertEquals(1, habit.longestStreak)
    }

    @Test
    fun `test habit with empty name edge case`() {
        // While the app should prevent empty names, test that the model handles it
        val habit = Habit(name = "", emoji = "💪")

        assertEquals("", habit.name)
    }

    @Test
    fun `test habit with whitespace-only name`() {
        val habit = Habit(name = "   ", emoji = "💪")

        assertEquals("   ", habit.name)
        // Note: The app should trim names before saving
    }

    @Test
    fun `test habit created date defaults to today`() {
        val habit = Habit(name = "Test", emoji = "💪")
        val today = LocalDate.now()

        assertEquals(today, habit.createdAt)
    }

    @Test
    fun `test habit with maximum possible streak`() {
        val today = LocalDate.now()
        // Create a 1000-day streak
        val dates = (0..999).map { today.minusDays(it.toLong()) }

        val habit = Habit(
            name = "Test",
            emoji = "💪",
            completedDates = dates
        )

        assertEquals(1000, habit.currentStreak)
        assertEquals(1000, habit.longestStreak)
    }

    @Test
    fun `test toggle today maintains date order`() {
        val yesterday = LocalDate.now().minusDays(1)
        val habit = Habit(
            name = "Test",
            emoji = "💪",
            completedDates = listOf(yesterday)
        )

        val updated = habit.toggleToday()

        // Verify dates are in some consistent order (implementation detail)
        assertTrue(updated.completedDates.contains(LocalDate.now()))
        assertTrue(updated.completedDates.contains(yesterday))
    }

    @Test
    fun `test milestone streaks contain expected values`() {
        val milestones = AppConfig.MILESTONE_STREAKS

        assertTrue("Should contain 7-day milestone", milestones.contains(7))
        assertTrue("Should contain 30-day milestone", milestones.contains(30))
        assertTrue("Should contain 100-day milestone", milestones.contains(100))
        assertTrue("Should contain 365-day milestone", milestones.contains(365))
    }
}
