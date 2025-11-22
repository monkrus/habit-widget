package com.habitstreak.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Enhanced confetti animation using Konfetti library
 * Supports different celebration types for various achievements
 */
@Composable
fun ConfettiAnimation(
    trigger: Int,
    celebrationType: CelebrationType = CelebrationType.HABIT_COMPLETED
) {
    var parties by remember { mutableStateOf<List<Party>>(emptyList()) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            Timber.d("Konfetti trigger changed to: $trigger, type: $celebrationType")
            parties = when (celebrationType) {
                CelebrationType.HABIT_COMPLETED -> createHabitCompletedParty()
                CelebrationType.MILESTONE_REACHED -> createMilestoneParty()
                CelebrationType.PERFECT_DAY -> createPerfectDayParty()
                CelebrationType.ACHIEVEMENT_UNLOCKED -> createAchievementParty()
                CelebrationType.STREAK_FREEZE -> createFreezeParty()
            }

            // Clear after animation
            kotlinx.coroutines.delay(3500)
            parties = emptyList()
            Timber.d("Konfetti animation finished")
        }
    }

    if (parties.isNotEmpty()) {
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = parties
        )
    }
}

/**
 * Types of celebrations with different confetti effects
 */
enum class CelebrationType {
    HABIT_COMPLETED,      // Standard completion - burst from center
    MILESTONE_REACHED,    // 7, 30, 100 day milestones - explosive
    PERFECT_DAY,          // All habits completed - rain from top
    ACHIEVEMENT_UNLOCKED, // New achievement - parade effect
    STREAK_FREEZE         // Streak protected - ice blue theme
}

/**
 * Standard habit completion - colorful burst from bottom center
 */
private fun createHabitCompletedParty(): List<Party> {
    val colors = listOf(
        0xFFFF6B6B.toInt(), // Red
        0xFF4ECDC4.toInt(), // Teal
        0xFFFFE66D.toInt(), // Yellow
        0xFF95E1D3.toInt(), // Mint
        0xFFF38181.toInt(), // Pink
        0xFFAA96DA.toInt(), // Purple
        0xFFFCBF49.toInt(), // Orange
        0xFF06FFA5.toInt()  // Green
    )

    return listOf(
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = colors,
            shapes = listOf(Shape.Square, Shape.Circle),
            sizes = listOf(Size.SMALL, Size.MEDIUM),
            position = Position.Relative(0.5, 0.5),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(60)
        )
    )
}

/**
 * Milestone celebration - bigger and more dramatic
 */
private fun createMilestoneParty(): List<Party> {
    val colors = listOf(
        0xFFFFD700.toInt(), // Gold
        0xFFFFA500.toInt(), // Orange
        0xFFFF6347.toInt(), // Tomato
        0xFFFF4500.toInt(), // OrangeRed
        0xFFFFE66D.toInt()  // Yellow
    )

    return listOf(
        // Center burst
        Party(
            speed = 0f,
            maxSpeed = 50f,
            damping = 0.9f,
            spread = 360,
            colors = colors,
            shapes = listOf(Shape.Square, Shape.Circle),
            sizes = listOf(Size.MEDIUM, Size.LARGE),
            position = Position.Relative(0.5, 0.5),
            emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(100)
        ),
        // Left fountain
        Party(
            speed = 10f,
            maxSpeed = 30f,
            damping = 0.9f,
            angle = 45,
            spread = 30,
            colors = colors,
            shapes = listOf(Shape.Square),
            sizes = listOf(Size.SMALL, Size.MEDIUM),
            position = Position.Relative(0.0, 1.0),
            emitter = Emitter(duration = 300, TimeUnit.MILLISECONDS).perSecond(50)
        ),
        // Right fountain
        Party(
            speed = 10f,
            maxSpeed = 30f,
            damping = 0.9f,
            angle = 135,
            spread = 30,
            colors = colors,
            shapes = listOf(Shape.Square),
            sizes = listOf(Size.SMALL, Size.MEDIUM),
            position = Position.Relative(1.0, 1.0),
            emitter = Emitter(duration = 300, TimeUnit.MILLISECONDS).perSecond(50)
        )
    )
}

/**
 * Perfect day - confetti rain from top
 */
private fun createPerfectDayParty(): List<Party> {
    val colors = listOf(
        0xFF4CAF50.toInt(), // Green
        0xFF8BC34A.toInt(), // Light Green
        0xFFCDDC39.toInt(), // Lime
        0xFFFFEB3B.toInt(), // Yellow
        0xFFFFD700.toInt()  // Gold
    )

    return listOf(
        Party(
            speed = 0f,
            maxSpeed = 15f,
            damping = 0.9f,
            angle = 270,
            spread = 90,
            colors = colors,
            shapes = listOf(Shape.Square, Shape.Circle),
            sizes = listOf(Size.SMALL, Size.MEDIUM, Size.LARGE),
            position = Position.Relative(0.5, 0.0),
            emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(80)
        )
    )
}

/**
 * Achievement unlocked - side parade effect
 */
private fun createAchievementParty(): List<Party> {
    val colors = listOf(
        0xFFE91E63.toInt(), // Pink
        0xFF9C27B0.toInt(), // Purple
        0xFF673AB7.toInt(), // Deep Purple
        0xFF3F51B5.toInt(), // Indigo
        0xFF2196F3.toInt()  // Blue
    )

    return listOf(
        // Left side
        Party(
            speed = 15f,
            maxSpeed = 25f,
            damping = 0.9f,
            angle = 0,
            spread = 60,
            colors = colors,
            shapes = listOf(Shape.Square, Shape.Circle),
            sizes = listOf(Size.MEDIUM),
            position = Position.Relative(0.0, 0.5),
            emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).perSecond(30)
        ),
        // Right side
        Party(
            speed = 15f,
            maxSpeed = 25f,
            damping = 0.9f,
            angle = 180,
            spread = 60,
            colors = colors,
            shapes = listOf(Shape.Square, Shape.Circle),
            sizes = listOf(Size.MEDIUM),
            position = Position.Relative(1.0, 0.5),
            emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).perSecond(30)
        )
    )
}

/**
 * Streak freeze - ice blue themed celebration
 */
private fun createFreezeParty(): List<Party> {
    val colors = listOf(
        0xFF00BCD4.toInt(), // Cyan
        0xFF03A9F4.toInt(), // Light Blue
        0xFF2196F3.toInt(), // Blue
        0xFF00E5FF.toInt(), // Cyan accent
        0xFFB3E5FC.toInt()  // Light blue 100
    )

    return listOf(
        Party(
            speed = 0f,
            maxSpeed = 20f,
            damping = 0.95f,
            spread = 360,
            colors = colors,
            shapes = listOf(Shape.Circle),
            sizes = listOf(Size.SMALL, Size.MEDIUM),
            position = Position.Relative(0.5, 0.5),
            emitter = Emitter(duration = 150, TimeUnit.MILLISECONDS).max(40)
        )
    )
}
