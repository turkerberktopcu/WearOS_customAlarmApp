package com.turkerberktopcu.customalarmapp.presentation.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.turkerberktopcu.customalarmapp.presentation.service.AlarmReceiver

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule an exact alarm at [timeInMillis].
     *
     * @param alarmId   Unique ID for the alarm (e.g., hour*100 + minute).
     * @param timeInMillis  The time in milliseconds (epoch) when the alarm should go off.
     * @param label     Optional label for the alarm.
     */
    fun scheduleAlarm(alarmId: Int, timeInMillis: Long, label: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // setExactAndAllowWhileIdle is often used for alarms that must ring even in Doze mode
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }

    /**
     * Cancel a previously scheduled alarm.
     *
     * @param alarmId  The same ID used to schedule.
     */
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
