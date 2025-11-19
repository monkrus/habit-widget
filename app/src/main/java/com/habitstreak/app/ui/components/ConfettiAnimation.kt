package com.habitstreak.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import timber.log.Timber
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Confetti particle data class
 */
data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val rotation: Float,
    val rotationSpeed: Float,
    val size: Float
)

/**
 * Confetti animation that bursts from the center
 */
@Composable
fun ConfettiAnimation(
    trigger: Int
) {
    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    var isAnimating by remember { mutableStateOf(false) }
    var animationStartTime by remember { mutableStateOf(0L) }

    // Use LaunchedEffect to start animation when triggered
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            Timber.d("Trigger changed to: $trigger, starting animation")
            isAnimating = true
            animationStartTime = System.currentTimeMillis()

            val colors = listOf(
                Color(0xFFFF6B6B), // Red
                Color(0xFF4ECDC4), // Teal
                Color(0xFFFFE66D), // Yellow
                Color(0xFF95E1D3), // Mint
                Color(0xFFF38181), // Pink
                Color(0xFFAA96DA), // Purple
                Color(0xFFFCBF49), // Orange
                Color(0xFF06FFA5)  // Green
            )

            particles = List(80) {
                val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
                val speed = Random.nextFloat() * 10 + 6

                ConfettiParticle(
                    x = 0.5f, // Start from center
                    y = 0.5f,
                    velocityX = cos(angle) * speed,
                    velocityY = sin(angle) * speed - 4, // Upward bias
                    color = colors.random(),
                    rotation = Random.nextFloat() * 360,
                    rotationSpeed = Random.nextFloat() * 20 - 10f,
                    size = Random.nextFloat() * 16 + 8
                )
            }

            Timber.d("Created ${particles.size} particles")

            // Clear after animation duration
            kotlinx.coroutines.delay(3000)
            particles = emptyList()
            isAnimating = false
            Timber.d("Animation finished")
        }
    }

    if (particles.isNotEmpty()) {
        // Continuous recomposition for animation
        var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

        LaunchedEffect(Unit) {
            while (isAnimating) {
                currentTime = System.currentTimeMillis()
                kotlinx.coroutines.delay(16) // ~60 FPS
            }
        }

        val progress = ((currentTime - animationStartTime) / 2500f).coerceIn(0f, 1f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val gravity = 0.15f

            particles.forEach { particle ->
                // Calculate position with physics
                val x = (particle.x * width) + (particle.velocityX * progress * 150)
                val y = (particle.y * height) +
                        (particle.velocityY * progress * 150) +
                        (gravity * progress * progress * 800)

                // Calculate rotation
                val rotation = particle.rotation + (particle.rotationSpeed * progress * 360)

                // Fade out in last 30% of animation
                val alpha = if (progress > 0.7f) {
                    (1f - (progress - 0.7f) / 0.3f).coerceIn(0f, 1f)
                } else {
                    1f
                }

                // Draw particle
                if (alpha > 0.01f) {
                    rotate(rotation, pivot = Offset(x, y)) {
                        drawRect(
                            color = particle.color.copy(alpha = alpha),
                            topLeft = Offset(x - particle.size / 2, y - particle.size / 2),
                            size = androidx.compose.ui.geometry.Size(particle.size, particle.size)
                        )
                    }
                }
            }
        }
    }
}
