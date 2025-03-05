package com.turkerberktopcu.customalarmapp.presentation.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler
import com.turkerberktopcu.customalarmapp.presentation.service.AlarmForegroundService
import com.turkerberktopcu.customalarmapp.presentation.theme.CustomAlarmAppTheme
import java.util.Calendar

class AlarmRingActivity : ComponentActivity() {

    private var alarmId: Int = -1
    private var alarmLabel: String = "Alarm"
    private lateinit var alarmManager: com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager
    private lateinit var alarmScheduler: AlarmScheduler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmId = intent.getIntExtra("ALARM_ID", -1)
        alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        alarmManager = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(this)
        alarmScheduler = AlarmScheduler(this)

        setupWindowFlags()
        setContent {
            CustomAlarmAppTheme {
                AlarmRingScreen(
                    label = alarmLabel,
                    onDismiss = ::handleDismiss,
                    onSnooze = ::handleSnooze,
                    onBack = ::navigateBack
                )
            }
        }
    }
    private fun handleDismiss() {
        val alarm = alarmManager.getAllAlarms().find { it.id == alarmId }

        alarm?.let {
            alarmManager.resetSnoozeCount(it.id)  // Reset on dismiss
            if (it.isDailyReset) {
                // Reschedule for tomorrow at same time
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, it.hour)
                    set(Calendar.MINUTE, it.minute)
                    set(Calendar.SECOND, 0)
                }

                alarmManager.apply {
                    updateAlarmTime(it.id, calendar.timeInMillis)
                    // Keep enabled and maintain daily reset setting
                    alarms.find { a -> a.id == it.id }?.isEnabled = true
                    saveAlarms()
                }
                alarmScheduler.scheduleAlarm(it.id, calendar.timeInMillis, it.label)
            } else {
                // Disable the alarm
                alarmManager.toggleAlarm(it.id)
                alarmScheduler.cancelAlarm(it.id)
            }
        }

        stopService(Intent(this, AlarmForegroundService::class.java))
        finish()
    }
    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }

    private fun handleSnooze() {
        val alarm = alarmManager.getAllAlarms().find { it.id == alarmId }
        val success = alarmManager.handleSnooze(alarmId)

        if (success && alarm != null) {
            // Use the alarm's saved snooze interval (in millis)
            val snoozeMillis = alarm.snoozeIntervalMillis
            val newAlarmTime = System.currentTimeMillis() + snoozeMillis
            alarmScheduler.scheduleAlarm(alarmId, newAlarmTime, alarmLabel)
        } else {
            Toast.makeText(this, "Max snooze reached", Toast.LENGTH_SHORT).show()
        }

        stopService(Intent(this, AlarmForegroundService::class.java))
        finish()
    }



    private fun navigateBack() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateBack()
    }
}
@Composable
fun AlarmRingScreen(
    label: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp) // Increased padding for better touch target
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface // Theme-adaptive color
            )
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp), // Space for back button
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge, // Consistent typography
                modifier = Modifier.padding(8.dp)
            )
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Full width with padding
            ) {
                Text(text = "Dismiss")
            }
            Spacer(modifier = Modifier.height(16.dp)) // Consistent spacing
            Button(
                onClick = onSnooze,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Full width with padding
            ) {
                Text(text = "Snooze")
            }
        }
    }
}


