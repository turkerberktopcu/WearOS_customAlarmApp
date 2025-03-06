package com.turkerberktopcu.customalarmapp.presentation.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.turkerberktopcu.customalarmapp.presentation.alarm.VibrationPattern
import com.turkerberktopcu.customalarmapp.presentation.presentation.AlarmRingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmForegroundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val alarmManagerInstance = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(this)
        val alarm = alarmManagerInstance.getAllAlarms().find { it.id == alarmId }

        // Start alarm sound and vibration as usual
        playAlarmSound()
        alarm?.let {
            startVibration(it.vibrationPattern)
        }

        // Build and start foreground notification as usual
        val notification = buildAlarmNotification(alarmId, alarmLabel)
        startForeground(alarmId, notification)

        // Auto-snooze: after workingDuration (e.g., 5 minutes), trigger snooze automatically
        // Use a coroutine for the delay
        alarm?.let { currentAlarm ->
            CoroutineScope(Dispatchers.Main).launch {
                delay(currentAlarm.workingDurationMillis)
                autoSnooze(alarmId, alarmLabel, currentAlarm.breakDurationMillis)
            }
        }

        return START_NOT_STICKY
    }
    private fun autoSnooze(alarmId: Int, alarmLabel: String, breakDurationMillis: Long) {
        val alarmManagerInstance = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(this)
        val alarmScheduler = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler(this)
        val alarm = alarmManagerInstance.getAllAlarms().find { it.id == alarmId }
        if (alarm != null) {
            val success = alarmManagerInstance.handleSnooze(alarmId)
            if (success) {
                val newAlarmTime = System.currentTimeMillis() + breakDurationMillis
                alarmScheduler.scheduleAlarm(alarmId, newAlarmTime, alarmLabel)
            }
        }
        stopForeground(true)
        stopSelf()
    }

    private fun startVibration(pattern: VibrationPattern?) {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        pattern?.getEffect(this)?.let { effect ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(effect)
            }
        }
    }
    private fun playAlarmSound() {
        try {
            var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(this@AlarmForegroundService, alarmUri)
                isLooping = true
                prepareAsync()
                setOnPreparedListener { start() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    private fun buildAlarmNotification(alarmId: Int, alarmLabel: String): Notification {
        val channelId = "alarm_channel_id_nosound"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val channel = NotificationChannel(
                channelId,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)

                vibrationPattern = longArrayOf(0, 100, 200, 300)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        // Create full-screen intent to open AlarmRingActivity (for dismiss/snooze UI)
        val fullScreenIntent = Intent(this, AlarmRingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,  // Use 0 to avoid conflicts with multiple alarms
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT  // Ensure intent updates properly
        )
        val contentIntent = Intent(this, AlarmRingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,  // Use unique ID for each alarm
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm")
            .setContentText(alarmLabel)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()

    }
}