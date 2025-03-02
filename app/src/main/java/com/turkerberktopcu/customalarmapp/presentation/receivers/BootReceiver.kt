package com.turkerberktopcu.customalarmapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmManager = AlarmManager(context)
            val alarmScheduler = AlarmScheduler(context)

            // Reschedule all enabled alarms
            alarmManager.getAllAlarms()
                .filter { it.isEnabled }
                .forEach { alarm ->
                    val updatedTime = alarmManager.calculateTriggerTime(alarm.hour, alarm.minute)
                    alarmScheduler.scheduleAlarm(alarm.id, updatedTime, alarm.label)
                }

            // Reschedule daily reset if enabled
            if (alarmManager.isDailyResetEnabled()) {
                alarmScheduler.scheduleDailyReset()
            }
        }
    }
}