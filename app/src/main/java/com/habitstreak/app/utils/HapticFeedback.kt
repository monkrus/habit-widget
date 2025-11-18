package com.habitstreak.app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticFeedback {

    /**
     * Haptic feedback for habit completion
     */
    fun habitCompleted(context: Context) {
        vibrate(context, 50)
    }

    /**
     * Haptic feedback for milestone achievements (stronger pattern)
     */
    fun milestoneReached(context: Context) {
        val timings = longArrayOf(0, 50, 50, 100)
        val amplitudes = intArrayOf(0, 128, 0, 255)
        vibratePattern(context, timings, amplitudes)
    }

    /**
     * Simple vibration
     */
    private fun vibrate(context: Context, duration: Long) {
        val vibrator = getVibrator(context) ?: return
        vibrator.vibrate(
            VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    /**
     * Pattern vibration for milestones
     */
    private fun vibratePattern(context: Context, timings: LongArray, amplitudes: IntArray) {
        val vibrator = getVibrator(context) ?: return
        vibrator.vibrate(
            VibrationEffect.createWaveform(timings, amplitudes, -1)
        )
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
}
