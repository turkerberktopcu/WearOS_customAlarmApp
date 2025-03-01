package com.turkerberktopcu.customalarmapp.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.turkerberktopcu.customalarmapp.presentation.presentation.AlarmRingActivity

class AlarmForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null
    @Suppress("WrongConstant")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Retrieve alarm extras
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"

        // Create a full-screen intent for the alarm UI
        val fullScreenIntent = Intent(this, AlarmRingActivity::class.java)
        fullScreenIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        fullScreenIntent.putExtra("ALARM_ID", alarmId)
        fullScreenIntent.putExtra("ALARM_LABEL", alarmLabel)

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification channel if needed
        val channelId = "alarm_channel_id"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for alarm notifications"
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Build the full-screen notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm")
            .setContentText("Your alarm is ringing!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        // Start the service in the foreground
        startForeground(alarmId, notification)

        // (Optional) If you want to immediately launch the alarm activity,
        // you can start it here as well.
        fullScreenPendingIntent.send()

        // If you want the service to continue running until explicitly stopped, return START_STICKY.
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up if needed.
    }
}
