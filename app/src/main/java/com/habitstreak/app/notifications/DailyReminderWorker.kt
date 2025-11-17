package com.habitstreak.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habitstreak.app.data.HabitRepository
import java.time.LocalDate

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = HabitRepository(applicationContext)
            val habits = repository.getHabits()

            // Count habits not completed today
            val today = LocalDate.now()
            val incompletedCount = habits.count { habit ->
                !habit.isCompletedToday
            }

            // Send reminder if there are incomplete habits
            if (incompletedCount > 0) {
                val notificationManager = HabitNotificationManager(applicationContext)
                notificationManager.sendDailyReminder(incompletedCount)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
