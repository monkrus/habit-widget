package com.habitstreak.app.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.habitstreak.app.MainActivity
import com.habitstreak.app.R
import com.habitstreak.app.data.HabitRepository
import com.habitstreak.app.data.TimeOfDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class HabitNotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "habit_reminders"
        private const val CHANNEL_NAME = "Habit Reminders"
        private const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_ID_MORNING = 1002
        const val NOTIFICATION_ID_AFTERNOON = 1003
        const val NOTIFICATION_ID_EVENING = 1004
        const val NOTIFICATION_ID_STREAK_RISK = 1005

        const val ACTION_MORNING_REMINDER = "com.habitstreak.app.MORNING_REMINDER"
        const val ACTION_AFTERNOON_REMINDER = "com.habitstreak.app.AFTERNOON_REMINDER"
        const val ACTION_EVENING_REMINDER = "com.habitstreak.app.EVENING_REMINDER"

        // Default reminder times
        const val MORNING_HOUR = 8
        const val AFTERNOON_HOUR = 13
        const val EVENING_HOUR = 20
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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

    /**
     * Schedule all time-based reminders
     */
    fun scheduleTimeBasedReminders() {
        scheduleReminder(TimeOfDay.MORNING, MORNING_HOUR, NOTIFICATION_ID_MORNING, ACTION_MORNING_REMINDER)
        scheduleReminder(TimeOfDay.AFTERNOON, AFTERNOON_HOUR, NOTIFICATION_ID_AFTERNOON, ACTION_AFTERNOON_REMINDER)
        scheduleReminder(TimeOfDay.EVENING, EVENING_HOUR, NOTIFICATION_ID_EVENING, ACTION_EVENING_REMINDER)
    }

    /**
     * Cancel all time-based reminders
     */
    fun cancelTimeBasedReminders() {
        cancelReminder(ACTION_MORNING_REMINDER, NOTIFICATION_ID_MORNING)
        cancelReminder(ACTION_AFTERNOON_REMINDER, NOTIFICATION_ID_AFTERNOON)
        cancelReminder(ACTION_EVENING_REMINDER, NOTIFICATION_ID_EVENING)
    }

    private fun scheduleReminder(timeOfDay: TimeOfDay, hour: Int, notificationId: Int, action: String) {
        val intent = Intent(context, TimeBasedReminderReceiver::class.java).apply {
            this.action = action
            putExtra("time_of_day", timeOfDay.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelReminder(action: String, notificationId: Int) {
        val intent = Intent(context, TimeBasedReminderReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Show notification for incomplete habits at a time of day
     */
    fun showTimeBasedReminder(timeOfDay: TimeOfDay, incompleteCount: Int) {
        if (incompleteCount == 0) return

        val notificationId = when (timeOfDay) {
            TimeOfDay.MORNING -> NOTIFICATION_ID_MORNING
            TimeOfDay.AFTERNOON -> NOTIFICATION_ID_AFTERNOON
            TimeOfDay.EVENING -> NOTIFICATION_ID_EVENING
            TimeOfDay.ANYTIME -> return
        }

        val title = "${timeOfDay.emoji} ${timeOfDay.displayName} Routine"
        val message = when (incompleteCount) {
            1 -> "You have 1 habit to complete!"
            else -> "You have $incompleteCount habits to complete!"
        }

        showNotification(notificationId, title, message)
    }

    /**
     * Show streak at risk notification
     */
    fun showStreakRiskNotification(habitName: String, streakDays: Int) {
        val title = "🔥 Streak at Risk!"
        val message = "Your $streakDays-day streak for \"$habitName\" needs attention!"
        showNotification(NOTIFICATION_ID_STREAK_RISK, title, message)
    }

    private fun showNotification(notificationId: Int, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // User hasn't granted notification permission
        }
    }

    fun sendDailyReminder(incompletedHabitsCount: Int) {
        if (incompletedHabitsCount == 0) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
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
            context, 0, intent,
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

/**
 * Broadcast receiver for time-based reminders
 */
class TimeBasedReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val timeOfDayName = intent.getStringExtra("time_of_day") ?: return
        val timeOfDay = try {
            TimeOfDay.valueOf(timeOfDayName)
        } catch (e: Exception) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val repository = HabitRepository(context)
            val habits = repository.getHabits()

            val incompleteCount = habits.count { habit ->
                habit.timeOfDay == timeOfDay && !habit.isCompletedToday && !habit.isFrozenToday
            }

            if (incompleteCount > 0) {
                val notificationManager = HabitNotificationManager(context)
                notificationManager.showTimeBasedReminder(timeOfDay, incompleteCount)
            }
        }
    }
}
