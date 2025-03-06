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

import kotlinx.coroutines.*

class AlarmRingActivity : ComponentActivity() {

    private var alarmId: Int = -1
    private var alarmLabel: String = "Alarm"
    private lateinit var alarmManager: com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager
    private lateinit var alarmScheduler: AlarmScheduler
    private var autoSnoozeJob: Job? = null  // Otomatik ertelemeyi kontrol etmek için

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

        // Alarm nesnesini alalım
        val alarm = alarmManager.getAllAlarms().find { it.id == alarmId }
        val workingDuration = alarm?.workingDurationMillis ?: 300_000L  // varsayılan 5 dk
        val breakDuration = alarm?.breakDurationMillis ?: 120_000L      // varsayılan 2 dk

        // Eğer kullanıcı müdahalesi olmazsa, otomatik ertelemeyi başlat
        autoSnoozeJob = CoroutineScope(Dispatchers.Main).launch {
            delay(workingDuration)
            // Eğer kullanıcı müdahalesi gerçekleşmemişse, otomatik erteleme işlemini tetikle
            handleAutoSnooze(breakDuration)
        }
    }

    private fun handleAutoSnooze(breakDuration: Long) {
        // Kullanıcı müdahalesi olmamışsa, alarmı otomatik olarak ertele (snooze)
        val alarm = alarmManager.getAllAlarms().find { it.id == alarmId }
        if (alarm != null) {
            // Burada handleSnooze metodunu kullanabilir veya kendi erteleme metodunuzu çağırabilirsiniz
            val success = alarmManager.handleSnooze(alarmId)
            if (success) {
                val newAlarmTime = System.currentTimeMillis() + breakDuration
                alarmScheduler.scheduleAlarm(alarmId, newAlarmTime, alarmLabel)
            }
        }
        // Alarmın sesini durdur ve aktiviteyi kapat
        stopService(Intent(this, AlarmForegroundService::class.java))
        finish()
    }

    private fun handleDismiss() {
        // Kullanıcı müdahalesinde otomatik erteleme zamanlayıcısını iptal edin
        autoSnoozeJob?.cancel()

        // Mevcut alarmı sonlandırma ve/veya yeniden programlama işlemleri...
        val alarm = alarmManager.getAllAlarms().find { it.id == alarmId }
        alarm?.let {
            alarmManager.resetSnoozeCount(it.id)
            if (it.isDailyReset) {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, it.hour)
                    set(Calendar.MINUTE, it.minute)
                    set(Calendar.SECOND, 0)
                }
                alarmManager.apply {
                    updateAlarmTime(it.id, calendar.timeInMillis)
                    alarms.find { a -> a.id == it.id }?.isEnabled = true
                    saveAlarms()
                }
                alarmScheduler.scheduleAlarm(it.id, calendar.timeInMillis, it.label)
            } else {
                alarmManager.toggleAlarm(it.id)
                alarmScheduler.cancelAlarm(it.id)
            }
        }
        stopService(Intent(this, AlarmForegroundService::class.java))
        finish()
    }

    private fun handleSnooze() {
        // Kullanıcı snooze'a bastığında otomatik erteleme zamanlayıcısını iptal edin
        autoSnoozeJob?.cancel()

        val alarm = alarmManager.getAllAlarms().find { it.id == alarmId }
        val success = alarmManager.handleSnooze(alarmId)
        if (success && alarm != null) {
            val snoozeMillis = alarm.snoozeIntervalMillis
            val newAlarmTime = System.currentTimeMillis() + snoozeMillis
            alarmScheduler.scheduleAlarm(alarmId, newAlarmTime, alarmLabel)
        } else {
            Toast.makeText(this, "Max snooze reached", Toast.LENGTH_SHORT).show()
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


