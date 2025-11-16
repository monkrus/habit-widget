package com.habitstreak.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitstreak.app.widget.HabitWidgetReceiver

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Update all widgets after device boot
            HabitWidgetReceiver.updateAllWidgets(context)
        }
    }
}
