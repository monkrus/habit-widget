package com.habitstreak.app.utils

/**
 * Application-wide configuration constants.
 * Centralizes all hardcoded values for easier maintenance and testing.
 */
object AppConfig {
    /**
     * Maximum number of habits allowed in the free version
     */
    const val FREE_HABIT_LIMIT = 3

    /**
     * Maximum number of habits displayed in the home screen widget
     */
    const val WIDGET_HABIT_LIMIT = 5

    /**
     * Maximum character length for habit names
     */
    const val HABIT_NAME_MAX_LENGTH = 30

    /**
     * Milestone streak values that trigger special feedback
     */
    val MILESTONE_STREAKS = listOf(7, 30, 100, 365)

    /**
     * Confetti animation particle count
     */
    const val CONFETTI_PARTICLE_COUNT = 80

    /**
     * Vibration duration for regular habit completion (milliseconds)
     */
    const val HAPTIC_DURATION_NORMAL = 200L

    /**
     * Vibration pattern for milestone achievements
     * Format: [delay, vibrate, delay, vibrate, ...]
     */
    val HAPTIC_PATTERN_MILESTONE = longArrayOf(0, 100, 50, 100, 50, 200)
}
