package com.turkerberktopcu.customalarmapp.presentation.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    private var autoSnoozeJob: Job? = null  // For auto snooze control

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

        val alarm = alarmManager.getAllAlarms().find { it.id == alarmId }
        val workingDuration = alarm?.workingDurationMillis ?: 300_000L  // Default 5 minutes
        val breakDuration = alarm?.breakDurationMillis ?: 120_000L      // Default 2 minutes

        // Start auto-snooze if the user does not interact
        autoSnoozeJob = CoroutineScope(Dispatchers.Main).launch {
            delay(workingDuration)
            handleAutoSnooze(breakDuration)
        }
    }

    private fun handleAutoSnooze(breakDuration: Long) {
        val alarm = alarmManager.getAllAlarms().find { it.id == alarmId }
        if (alarm != null) {
            val success = alarmManager.handleSnooze(alarmId)
            if (success) {
                val newAlarmTime = System.currentTimeMillis() + breakDuration
                alarmScheduler.scheduleAlarm(alarmId, newAlarmTime, alarmLabel)
            }
        }
        stopService(Intent(this, AlarmForegroundService::class.java))
        finish()
    }

    private fun handleDismiss() {
        autoSnoozeJob?.cancel()
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
    val wearMaterialTheme = androidx.wear.compose.material.MaterialTheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        androidx.wear.compose.material.Scaffold(
            timeText = { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    androidx.wear.compose.material.Text(
                        text = label,
                        style = wearMaterialTheme.typography.caption1,
                        color = Color.White
                    )
                }
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
                val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')

                androidx.wear.compose.material.Text(
                    text = "$hour:$minute",
                    color = Color.White,
                    style = wearMaterialTheme.typography.display1
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Dismiss slider
                    MinimalistSliderDismiss(onDismiss = onDismiss)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Snooze slider ("Ertele") with yellowish knob
                    MinimalistSliderSnooze(onSnooze = onSnooze)
                }
            }
        }
    }
}

@Composable
fun MinimalistSliderDismiss(onDismiss: () -> Unit) {
    var offsetX by remember { mutableStateOf(0f) }
    val dismissThreshold = 100.dp       // Reduced threshold
    val sliderWidth = 150.dp            // Smaller slider width
    val sliderHeight = 36.dp            // Keep slider height
    val knobWidth = 40.dp               // Smaller knob width

    val dismissThresholdPx = with(LocalDensity.current) { dismissThreshold.toPx() }
    val sliderWidthPx = with(LocalDensity.current) { sliderWidth.toPx() }
    val knobWidthPx = with(LocalDensity.current) { knobWidth.toPx() }

    Box(
        modifier = Modifier
            .width(sliderWidth)
            .height(sliderHeight)
            .background(
                color = Color(0x33FFFFFF),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        // Track text for dismiss slider
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Kapat",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            )
        }

        // Slider knob for dismiss slider (green)
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .width(knobWidth)
                .fillMaxHeight()
                .background(
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(18.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (offsetX > dismissThresholdPx) {
                                onDismiss()
                            } else {
                                offsetX = 0f
                            }
                        },
                        onDragCancel = { offsetX = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount.x).coerceIn(0f, sliderWidthPx - knobWidthPx)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Kaydır",
                tint = Color.White,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer(rotationZ = 180f)
            )
        }
    }
}

@Composable
fun MinimalistSliderSnooze(onSnooze: () -> Unit) {
    var offsetX by remember { mutableStateOf(0f) }
    val snoozeThreshold = 100.dp       // Threshold for snooze slider
    val sliderWidth = 150.dp           // Slider width
    val sliderHeight = 36.dp           // Slider height
    val knobWidth = 40.dp              // Knob width

    val snoozeThresholdPx = with(LocalDensity.current) { snoozeThreshold.toPx() }
    val sliderWidthPx = with(LocalDensity.current) { sliderWidth.toPx() }
    val knobWidthPx = with(LocalDensity.current) { knobWidth.toPx() }

    Box(
        modifier = Modifier
            .width(sliderWidth)
            .height(sliderHeight)
            .background(
                color = Color(0x33FFFFFF),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        // Track text for snooze slider (with yellowish tint)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Ertele",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFFFFEB3B)
                )
            )
        }

        // Slider knob for snooze slider (yellowish)
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .width(knobWidth)
                .fillMaxHeight()
                .background(
                    color = Color(0xFFFFEB3B), // Yellowish knob
                    shape = RoundedCornerShape(18.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (offsetX > snoozeThresholdPx) {
                                onSnooze()
                            } else {
                                offsetX = 0f
                            }
                        },
                        onDragCancel = { offsetX = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount.x).coerceIn(0f, sliderWidthPx - knobWidthPx)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Kaydır",
                tint = Color.White,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer(rotationZ = 180f)
            )
        }
    }
}
