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
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.turkerberktopcu.customalarmapp.presentation.alarm.Alarm
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

        // Start alarm sound and continuous vibration
        playAlarmSound(alarm)
        alarm?.let {
            startVibration(it.vibrationPattern)
        }

        // Build and start foreground notification
        val notification = buildAlarmNotification(alarmId, alarmLabel)
        startForeground(alarmId, notification)

        // Auto-snooze: after workingDuration, trigger snooze automatically
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

    /**
     * This updated startVibration() method creates a repeating vibration waveform.
     * The pattern array defines: initial delay, vibration duration, pause duration.
     * The repeat index of 0 tells the vibrator to repeat the entire pattern continuously.
     */
    private fun startVibration(pattern: VibrationPattern?) {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (vibrator?.hasVibrator() != true) {
            Log.w("AlarmForegroundService", "Device does not support vibration")
            return
        }

        // Define a repeating vibration pattern based on the selected pattern.
        val waveform: LongArray = when (pattern) {
            VibrationPattern.Default -> longArrayOf(0, 500, 500)  // Vibrate 500ms, pause 500ms, repeat
            VibrationPattern.Short   -> longArrayOf(0, 200, 200)  // Vibrate 200ms, pause 200ms, repeat
            VibrationPattern.Long    -> longArrayOf(0, 1000, 500) // Vibrate 1000ms, pause 500ms, repeat
            VibrationPattern.Custom  -> longArrayOf(0, 1000, 500) // Customize as needed
            VibrationPattern.None    -> return  // Do not vibrate
            else                     -> longArrayOf(0, 500, 500)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // The repeat index (0) indicates that the pattern should repeat from the beginning.
            val effect = VibrationEffect.createWaveform(waveform, 0)
            vibrator?.vibrate(effect)
        } else {
            // For pre-Oreo devices, pass the repeat index as the second parameter.
            vibrator?.vibrate(waveform, 0)
        }
    }

    private fun playAlarmSound(alarm: Alarm?) {
        try {
            // If "No Sound" is selected, do not play any sound.
            if (alarm?.alarmSoundUri == "NO_SOUND") {
                return
            }
            val alarmUri = alarm?.alarmSoundUri?.let { Uri.parse(it) }
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

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

        val fullScreenIntent = Intent(this, AlarmRingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        val contentIntent = Intent(this, AlarmRingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
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
