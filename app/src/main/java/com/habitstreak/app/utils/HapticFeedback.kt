package com.habitstreak.app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object HapticFeedback {

    private const val TAG = "HapticFeedback"

    /**
     * Haptic feedback for habit completion
     */
    fun habitCompleted(context: Context) {
        Log.d(TAG, "habitCompleted called")
        vibrate(context, 50)
    }

    /**
     * Haptic feedback for milestone achievements (stronger pattern)
     */
    fun milestoneReached(context: Context) {
        Log.d(TAG, "milestoneReached called")
        val timings = longArrayOf(0, 50, 50, 100)
        val amplitudes = intArrayOf(0, 128, 0, 255)
        vibratePattern(context, timings, amplitudes)
    }

    /**
     * Simple vibration
     */
    private fun vibrate(context: Context, duration: Long) {
        val vibrator = getVibrator(context)
        if (vibrator == null) {
            Log.e(TAG, "Vibrator is null")
            return
        }

        if (!vibrator.hasVibrator()) {
            Log.e(TAG, "Device has no vibrator")
            return
        }

        Log.d(TAG, "Vibrating for ${duration}ms")
        try {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
            Log.d(TAG, "Vibration triggered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating", e)
        }
    }

    /**
     * Pattern vibration for milestones
     */
    private fun vibratePattern(context: Context, timings: LongArray, amplitudes: IntArray) {
        val vibrator = getVibrator(context)
        if (vibrator == null) {
            Log.e(TAG, "Vibrator is null for pattern")
            return
        }

        if (!vibrator.hasVibrator()) {
            Log.e(TAG, "Device has no vibrator for pattern")
            return
        }

        Log.d(TAG, "Vibrating pattern")
        try {
            vibrator.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            )
            Log.d(TAG, "Pattern vibration triggered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating pattern", e)
        }
    }

    /**
     * Get vibrator service
     */
    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                Log.d(TAG, "Got vibrator via VibratorManager (Android 12+): $vibrator")
                vibrator
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                Log.d(TAG, "Got vibrator via VIBRATOR_SERVICE (legacy): $vibrator")
                vibrator
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting vibrator", e)
            null
        }
    }
}
