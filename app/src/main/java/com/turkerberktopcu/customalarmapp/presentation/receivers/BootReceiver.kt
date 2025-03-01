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

            alarmManager.getAllAlarms()
                .filter { it.isEnabled }
                .forEach { alarm ->
                    // Recalculate time in case we crossed midnight during reboot
                    val updatedTime = alarmManager.calculateTriggerTime(alarm.hour, alarm.minute)
                    alarmScheduler.scheduleAlarm(alarm.id, updatedTime, alarm.label)
                }
        }
    }
}