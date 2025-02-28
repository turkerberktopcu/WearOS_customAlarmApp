package com.turkerberktopcu.customalarmapp.presentation.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.turkerberktopcu.customalarmapp.presentation.presentation.AlarmRingActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"

        // Option A: Directly start the AlarmRingActivity in a new task
        val ringIntent = Intent(context, AlarmRingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
        }
        context.startActivity(ringIntent)

        // Option B: You could also start a ForegroundService or show a notification
        // to trigger a full-screen intent. For simplicity, we start the activity.
    }
}
