package com.habitstreak.app

import com.habitstreak.app.data.Achievement
import com.habitstreak.app.data.AchievementChecker
import com.habitstreak.app.data.Habit
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for Achievement system
 */
class AchievementTest {

    @Test
    fun `test FIRST_STEP achievement for streak of 1`() {
        val achievements = AchievementChecker.checkStreakAchievements(1)

        assertTrue(achievements.contains(Achievement.FIRST_STEP))
        assertEquals(1, achievements.size)
    }

    @Test
    fun `test WEEK_WARRIOR achievement for streak of 7`() {
        val achievements = AchievementChecker.checkStreakAchievements(7)

        assertTrue(achievements.contains(Achievement.WEEK_WARRIOR))
    }

    @Test
    fun `test MONTH_MASTER achievement for streak of 30`() {
        val achievements = AchievementChecker.checkStreakAchievements(30)

        assertTrue(achievements.contains(Achievement.MONTH_MASTER))
    }

    @Test
    fun `test CENTURY_CLUB achievement for streak of 100`() {
        val achievements = AchievementChecker.checkStreakAchievements(100)

        assertTrue(achievements.contains(Achievement.CENTURY_CLUB))
    }

    @Test
    fun `test YEAR_LEGEND achievement for streak of 365`() {
        val achievements = AchievementChecker.checkStreakAchievements(365)

        assertTrue(achievements.contains(Achievement.YEAR_LEGEND))
    }

    @Test
    fun `test no achievements for streak less than 1`() {
        val achievements = AchievementChecker.checkStreakAchievements(0)

        assertTrue(achievements.isEmpty())
    }

    @Test
    fun `test HABIT_STARTER achievement for 1 habit`() {
        val achievements = AchievementChecker.checkHabitCountAchievements(1)

        assertTrue(achievements.contains(Achievement.HABIT_STARTER))
        assertEquals(1, achievements.size)
    }

    @Test
    fun `test HABIT_COLLECTOR achievement for 5 habits`() {
        val achievements = AchievementChecker.checkHabitCountAchievements(5)

        assertTrue(achievements.contains(Achievement.HABIT_COLLECTOR))
    }

    @Test
    fun `test HABIT_MASTER achievement for 10 habits`() {
        val achievements = AchievementChecker.checkHabitCountAchievements(10)

        assertTrue(achievements.contains(Achievement.HABIT_MASTER))
    }

    @Test
    fun `test PERFECT_WEEK achievement with all habits completed for 7 days`() {
        val today = LocalDate.now()
        val habits = listOf(
            Habit(
                name = "Exercise",
                emoji = "💪",
                completedDates = (0..6).map { today.minusDays(it.toLong()) }
            ),
            Habit(
                name = "Read",
                emoji = "📚",
                completedDates = (0..6).map { today.minusDays(it.toLong()) }
            )
        )

        val isPerfectWeek = AchievementChecker.checkPerfectWeekAchievement(habits)

        assertTrue(isPerfectWeek)
    }

    @Test
    fun `test PERFECT_WEEK fails when one habit missed a day`() {
        val today = LocalDate.now()
        val habits = listOf(
            Habit(
                name = "Exercise",
                emoji = "💪",
                completedDates = (0..6).map { today.minusDays(it.toLong()) }
            ),
            Habit(
                name = "Read",
                emoji = "📚",
                completedDates = (0..5).map { today.minusDays(it.toLong()) } // Only 6 days
            )
        )

        val isPerfectWeek = AchievementChecker.checkPerfectWeekAchievement(habits)

        assertFalse(isPerfectWeek)
    }

    @Test
    fun `test PERFECT_MONTH achievement with all habits completed for 30 days`() {
        val today = LocalDate.now()
        val habits = listOf(
            Habit(
                name = "Exercise",
                emoji = "💪",
                completedDates = (0..29).map { today.minusDays(it.toLong()) }
            )
        )

        val isPerfectMonth = AchievementChecker.checkPerfectMonthAchievement(habits)

        assertTrue(isPerfectMonth)
    }

    @Test
    fun `test PERFECT_MONTH fails when not all days completed`() {
        val today = LocalDate.now()
        val habits = listOf(
            Habit(
                name = "Exercise",
                emoji = "💪",
                completedDates = (0..28).map { today.minusDays(it.toLong()) } // Only 29 days
            )
        )

        val isPerfectMonth = AchievementChecker.checkPerfectMonthAchievement(habits)

        assertFalse(isPerfectMonth)
    }

    @Test
    fun `test achievement IDs are unique`() {
        val allAchievements = Achievement.values().toList()
        val ids = allAchievements.map { it.id }
        val uniqueIds = ids.toSet()

        assertEquals(ids.size, uniqueIds.size)
    }

    @Test
    fun `test all achievements have non-empty titles`() {
        Achievement.values().forEach { achievement ->
            assertTrue(achievement.title.isNotBlank())
        }
    }

    @Test
    fun `test all achievements have non-empty descriptions`() {
        Achievement.values().forEach { achievement ->
            assertTrue(achievement.description.isNotBlank())
        }
    }
}
