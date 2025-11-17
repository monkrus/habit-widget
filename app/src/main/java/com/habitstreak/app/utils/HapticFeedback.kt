package com.habitstreak.app.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.habitstreak.app.R

object HapticFeedback {

    private var soundPool: SoundPool? = null
    private var completionSoundId: Int = -1
    private var milestoneSoundId: Int = -1
    private var isInitialized = false

    /**
     * Initialize sound pool for audio feedback
     */
    fun initialize(context: Context) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load sound effects
        try {
            completionSoundId = soundPool?.load(context, R.raw.completion_sound, 1) ?: -1
            milestoneSoundId = soundPool?.load(context, R.raw.milestone_sound, 1) ?: -1
        } catch (e: Exception) {
            // Sounds not found, that's okay - they're optional
        }

        isInitialized = true
    }

    /**
     * Haptic feedback for habit completion
     */
    fun habitCompleted(context: Context) {
        vibrate(context, 50, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    /**
     * Haptic feedback for milestone achievements (stronger)
     */
    fun milestoneReached(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 50, 50, 100)
            val amplitudes = intArrayOf(0, 128, 0, 255)
            vibrate(context, timings, amplitudes)
        } else {
            vibrate(context, 100, VibrationEffect.DEFAULT_AMPLITUDE)
        }
    }

    /**
     * Play completion sound
     */
    fun playCompletionSound() {
        if (completionSoundId != -1) {
            soundPool?.play(completionSoundId, 0.5f, 0.5f, 1, 0, 1f)
        }
    }

    /**
     * Play milestone celebration sound
     */
    fun playMilestoneSound() {
        if (milestoneSoundId != -1) {
            soundPool?.play(milestoneSoundId, 0.7f, 0.7f, 1, 0, 1f)
        }
    }

    /**
     * Basic vibration
     */
    private fun vibrate(context: Context, duration: Long, amplitude: Int) {
        val vibrator = getVibrator(context) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, amplitude)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    /**
     * Pattern vibration
     */
    private fun vibrate(context: Context, timings: LongArray, amplitudes: IntArray) {
        val vibrator = getVibrator(context) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            )
        }
    }

    /**
     * Get vibrator service
     */
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Release resources
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        isInitialized = false
    }
}
