package com.turkerberktopcu.customalarmapp.presentation.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.turkerberktopcu.customalarmapp.presentation.receivers.AlarmReceiver
import com.turkerberktopcu.customalarmapp.presentation.utils.Constants
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarmId: Int, triggerAtMillis: Long, label: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canSchedule = alarmManager.canScheduleExactAlarms()
            if (!canSchedule) {
                // Return false to indicate need for permission
                return
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,  // Use unique alarmId
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun scheduleDailyReset() {
        val alarmManager =
            com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(context)
        if (!alarmManager.isDailyResetEnabled()) return

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        scheduleAlarm(
            Constants.DAILY_RESET_ALARM_ID,
            calendar.timeInMillis,
            "DAILY_RESET"
        )
    }

    fun cancelDailyReset() {
        cancelAlarm(Constants.DAILY_RESET_ALARM_ID)
    }

    fun cancelAlarm(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
