package com.habitstreak.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.habitstreak.app.MainActivity
import com.habitstreak.app.R
import com.habitstreak.app.data.Habit
import com.habitstreak.app.data.HabitRepository
import com.habitstreak.app.utils.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HabitWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_HABIT_CLICKED) {
            val habitId = intent.getStringExtra(EXTRA_HABIT_ID)
            if (habitId != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val repository = HabitRepository(context)
                    repository.toggleHabitToday(habitId)

                    // Update all widgets
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val ids = appWidgetManager.getAppWidgetIds(
                        ComponentName(context, HabitWidgetReceiver::class.java)
                    )
                    for (id in ids) {
                        updateAppWidget(context, appWidgetManager, id)
                    }
                }
            }
        }
    }

    companion object {
        private const val ACTION_HABIT_CLICKED = "com.habitstreak.app.HABIT_CLICKED"
        private const val EXTRA_HABIT_ID = "habit_id"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val repository = HabitRepository(context)
                val habits = repository.getHabits().sortedBy { it.position }.take(AppConfig.WIDGET_HABIT_LIMIT)

                val views = RemoteViews(context.packageName, R.layout.widget_layout)

                // Clear previous habits
                views.removeAllViews(R.id.habits_container)

                if (habits.isEmpty()) {
                    views.setViewVisibility(R.id.empty_message, View.VISIBLE)
                    views.setViewVisibility(R.id.habits_container, View.GONE)
                } else {
                    views.setViewVisibility(R.id.empty_message, View.GONE)
                    views.setViewVisibility(R.id.habits_container, View.VISIBLE)

                    for (habit in habits) {
                        val habitView = createHabitView(context, habit)
                        views.addView(R.id.habits_container, habitView)
                    }
                }

                // Set click intent for the widget title to open the app
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun createHabitView(context: Context, habit: Habit): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_habit_item)

            views.setTextViewText(R.id.habit_emoji, habit.emoji)
            views.setTextViewText(R.id.habit_name, habit.name)

            val streakText = if (habit.isCompletedToday) {
                "✓"
            } else {
                "🔥 ${habit.currentStreak}"
            }
            views.setTextViewText(R.id.habit_streak, streakText)

            // Set click listener
            val clickIntent = Intent(context, HabitWidgetReceiver::class.java).apply {
                action = ACTION_HABIT_CLICKED
                putExtra(EXTRA_HABIT_ID, habit.id)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                habit.id.hashCode(),
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.habit_item_container, pendingIntent)

            return views
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, HabitWidgetReceiver::class.java)
            )
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
