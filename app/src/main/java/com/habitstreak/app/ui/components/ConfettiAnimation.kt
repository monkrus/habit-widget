package com.habitstreak.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
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
    trigger: Boolean,
    onFinished: () -> Unit = {}
) {
    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")

    // Animation progress
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    // Generate particles when triggered
    LaunchedEffect(trigger) {
        if (trigger) {
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

            particles = List(30) {
                val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
                val speed = Random.nextFloat() * 5 + 3

                ConfettiParticle(
                    x = 0.5f, // Start from center
                    y = 0.5f,
                    velocityX = cos(angle) * speed,
                    velocityY = sin(angle) * speed - 2, // Slight upward bias
                    color = colors.random(),
                    rotation = Random.nextFloat() * 360,
                    rotationSpeed = Random.nextFloat() * 10 - 5,
                    size = Random.nextFloat() * 8 + 4
                )
            }

            // Clear particles after animation
            kotlinx.coroutines.delay(2000)
            particles = emptyList()
            onFinished()
        }
    }

    if (particles.isNotEmpty()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val gravity = 0.2f

            particles.forEach { particle ->
                val progress = animationProgress

                // Calculate position with physics
                val x = (particle.x * width) + (particle.velocityX * progress * 100)
                val y = (particle.y * height) +
                        (particle.velocityY * progress * 100) +
                        (gravity * progress * progress * 500)

                // Calculate rotation
                val rotation = particle.rotation + (particle.rotationSpeed * progress * 360)

                // Fade out towards the end
                val alpha = (1f - progress).coerceIn(0f, 1f)

                // Only draw if still visible on screen
                if (x in 0f..width && y in 0f..height) {
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
