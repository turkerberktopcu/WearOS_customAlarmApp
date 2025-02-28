package com.turkerberktopcu.customalarmapp.presentation.presentation

import android.media.RingtoneManager
import android.net.Uri
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler
import com.turkerberktopcu.customalarmapp.presentation.theme.CustomAlarmAppTheme
import java.util.Calendar

class AlarmRingActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var alarmId: Int = -1
    private var alarmLabel: String = "Alarm"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract extras
        alarmId = intent.getIntExtra("ALARM_ID", -1)
        alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"

        // Play an alarm sound from res/raw/alarm_sound.mp3 (create it in your project)
        // or use RingtoneManager to get default alarm sound.
        // For example, if you put an mp3 in res/raw/alarm_sound.mp3:
        // mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
        // For now, let's assume you have alarm_sound in raw folder:
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer.create(this, alarmUri)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        setContent {
            CustomAlarmAppTheme {
                AlarmRingScreen(
                    label = alarmLabel,
                    onDismiss = {
                        finish() // simply close
                    },
                    onSnooze = {
                        // Example: Snooze for 5 minutes
                        val snoozeMillis = 5 * 60 * 1000
                        val newAlarmTime = System.currentTimeMillis() + snoozeMillis

                        AlarmScheduler(this).scheduleAlarm(
                            alarmId = alarmId,    // re-use same ID or use a new one
                            timeInMillis = newAlarmTime,
                            label = alarmLabel
                        )
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

@Composable
fun AlarmRingScreen(
    label: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    // A simple UI to show the alarm label and two buttons
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onDismiss) {
                Text(text = "Dismiss")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSnooze) {
                Text(text = "Snooze")
            }
        }
    }
}
