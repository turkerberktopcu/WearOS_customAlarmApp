package com.turkerberktopcu.customalarmapp.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler
import com.turkerberktopcu.customalarmapp.presentation.service.AlarmForegroundService
import com.turkerberktopcu.customalarmapp.presentation.utils.Constants.DAILY_RESET_ALARM_ID
import com.turkerberktopcu.customalarmapp.presentation.utils.Constants.INVALID_ALARM_ID

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", INVALID_ALARM_ID)

        when (alarmId) {
            DAILY_RESET_ALARM_ID -> handleDailyReset(context)
            INVALID_ALARM_ID -> Log.e("AlarmReceiver", "Received invalid alarm ID")
            else -> handleRegularAlarm(context, intent)
        }
    }

    private fun handleRegularAlarm(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", INVALID_ALARM_ID)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"

        Log.d("AlarmReceiver", "Triggering alarm ID: $alarmId - $alarmLabel")

        val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun handleDailyReset(context: Context) {
        Log.d("AlarmReceiver", "Daily reset triggered")

        val alarmManager = AlarmManager(context)
        val alarmScheduler = AlarmScheduler(context)

        // Reschedule all enabled alarms
        alarmManager.getAllAlarms()
            .filter { it.isEnabled }
            .forEach { alarm ->
                val newTime = alarmManager.calculateTriggerTime(alarm.hour, alarm.minute)
                Log.d("DailyReset", "Rescheduling alarm ${alarm.id} for ${alarm.hour}:${alarm.minute}")
                alarmScheduler.scheduleAlarm(alarm.id, newTime, alarm.label)
            }

        // Schedule next daily reset
        alarmScheduler.scheduleDailyReset()
    }
}