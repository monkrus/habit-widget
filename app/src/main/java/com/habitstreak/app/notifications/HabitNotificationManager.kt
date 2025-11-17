package com.habitstreak.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.habitstreak.app.MainActivity
import com.habitstreak.app.R

class HabitNotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "habit_reminders"
        private const val CHANNEL_NAME = "Habit Reminders"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminders to complete your habits"
            enableVibration(true)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun sendDailyReminder(incompletedHabitsCount: Int) {
        if (incompletedHabitsCount == 0) return // Don't notify if all habits done

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Don't break the chain! 🔥"
        val message = when (incompletedHabitsCount) {
            1 -> "You have 1 habit left to complete today"
            else -> "You have $incompletedHabitsCount habits left to complete today"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // User hasn't granted notification permission
        }
    }

    fun sendStreakCelebration(habitName: String, streak: Int) {
        val milestones = listOf(7, 30, 100, 365)
        if (streak !in milestones) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emoji = when (streak) {
            7 -> "🎉"
            30 -> "🏆"
            100 -> "💎"
            365 -> "👑"
            else -> "⭐"
        }

        val title = "$emoji $streak Day Streak!"
        val message = "Amazing! You've completed \"$habitName\" for $streak days straight!"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(streak, notification)
        } catch (e: SecurityException) {
            // User hasn't granted notification permission
        }
    }
}
